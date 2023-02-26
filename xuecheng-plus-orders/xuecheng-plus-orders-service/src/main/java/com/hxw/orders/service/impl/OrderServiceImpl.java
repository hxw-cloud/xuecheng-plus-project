package com.hxw.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.base.utils.IdWorkerUtils;
import com.hxw.base.utils.QRCodeUtil;
import com.hxw.messagesdk.service.MqMessageService;
import com.hxw.orders.config.PayNotifyConfig;
import com.hxw.orders.mapper.XcOrdersGoodsMapper;
import com.hxw.orders.mapper.XcOrdersMapper;
import com.hxw.orders.mapper.XcPayRecordMapper;
import com.hxw.orders.model.dto.AddOrderDto;
import com.hxw.orders.model.dto.PayRecordDto;
import com.hxw.orders.model.dto.PayStatusDto;
import com.hxw.orders.model.po.XcOrders;
import com.hxw.orders.model.po.XcOrdersGoods;
import com.hxw.orders.model.po.XcPayRecord;
import com.hxw.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Autowired
    OrderServiceImpl proxy;
    @Autowired
    private MqMessageService mqMessageService;
    @Resource
    private XcOrdersMapper ordersMapper;

    @Resource
    private XcOrdersGoodsMapper ordersGoodsMapper;

    @Resource
    private XcPayRecordMapper payRecordMapper;


    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //创建商品订单
        XcOrders xcOrders = proxy.saveXcOrders(userId, addOrderDto);

        //生成商品支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);

        //返回商品二维码
        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            qrCode = new QRCodeUtil()
                    .createQRCode("http://192.168.3.160/api/orders/requestpay?payNo=" +
                            payRecord.getPayNo(), 200, 200);

        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {

        XcPayRecord payRecord = payRecordMapper.selectOne(
                new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));

        return payRecord;
    }

    @Override
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //只有支付成功才支付订单
        if (payStatusDto.getTrade_status().equals("TRADE_SUCCESS")) {

            //根据支付记录交易号查询支付记录
            String payNo = payStatusDto.getOut_trade_no();
            XcPayRecord xcPayRecord = getPayRecordByPayno(payNo);
            if (xcPayRecord == null) {
                log.info("收到支付结果通知,查询不到支付订单，收到的信息:{}", payStatusDto);
                return;
            }
            //检查是否已经支付

            if ("601002".equals(xcPayRecord.getStatus())) {
                log.info("收到支付结果通知,支付订单已经更新:{}", payStatusDto);
                return;
            }

            //appid检查
            String appId_alipay = payStatusDto.getApp_id();

            //支付记录表中的总金额校验
            Float totalPriceDb = xcPayRecord.getTotalPrice() * 100;

            int total_amount = (int) (Float.parseFloat(payStatusDto.getTotal_amount()) * 100);


            if (totalPriceDb.intValue() != total_amount || !appId_alipay.equals(APP_ID)) {
                log.info("收到的支付结果通知，校验失败，不继续进行");
                log.info("支付宝数据:支付宝appid:{},支付宝金额:{}", appId_alipay, payStatusDto.getTotal_amount());
                log.info("数据库数据:我们的appid:{},数据库金额:{}", APP_ID, xcPayRecord.getTotalPrice());
                return;
            }


            //更新支付记录

            XcPayRecord xcPayRecord_u = new XcPayRecord();
            xcPayRecord_u.setStatus("601002");//支付成功
            xcPayRecord_u.setOutPayNo(payStatusDto.getTrade_no());//支付宝自己的订单号
            xcPayRecord_u.setOutPayChannel("603002");

            int update = payRecordMapper.update(xcPayRecord_u,
                    new LambdaQueryWrapper<XcPayRecord>()
                            .eq(XcPayRecord::getPayNo, payNo));

            if (update > 0) {
                log.info("收到支付宝支付结果通知，更新支付记录表成功:{}", payStatusDto);
            } else {
                log.info("收到支付宝支付结果通知，更新支付记录表失败:{}", payStatusDto);
            }
            //从支付记录中拿到订单号，查询订单

            XcOrders xcOrders = ordersMapper.selectById(xcPayRecord.getOrderId());
            if (xcOrders == null) {


                log.info("收到支付宝支付结果通知,查询不到订单,支付宝传递的参数:{},支付的订单号为:{}",
                        payStatusDto, xcPayRecord.getOrderId());

                return;
            }

            XcOrders xcOrders_u = new XcOrders();

            //更新订单的状态
            xcOrders_u.setStatus("600002");//更新订单支付状态为支付成功

            int update1 = ordersMapper.update(xcOrders_u,
                    new LambdaQueryWrapper<XcOrders>()
                            .eq(XcOrders::getId, xcPayRecord.getOrderId()));

            if (update1 > 0) {
                //订单表所关联的外部业务系统主键
                String businessId = xcOrders.getOutBusinessId();
                //向消息表插入记录
                mqMessageService.addMessage(PayNotifyConfig.MESSAGE_TYPE, businessId,
                        xcOrders.getOrderType(), null);


                log.info("收到支付宝支付结果通知，更新订单表成功:{}", payStatusDto);
            } else {
                log.info("收到支付宝支付结果通知，更新订单表失败:{}", payStatusDto);
            }


        }
    }

    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {

        //获取选课记录的Id
        String businessId = addOrderDto.getOutBusinessId();

        //对订单的插入进行幂等性处理
        //根据选课记录Id对数据库进行查询订单信息
        XcOrders order = getOrderByBusinessId(businessId);
        if (order != null) {
            return order;
        }
        //添加订单
        order = new XcOrders();
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(order);
        //插入订单明细表
        String orderDetail = addOrderDto.getOrderDetail();
        //将json转成list
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        //将明细list插入数据库
        xcOrdersGoods.forEach(good -> {
            //将订单号插入订单明细表
            good.setOrderId(orderId);
            ordersGoodsMapper.insert(good);
        });

        return order;

    }

    //添加支付记录
    public XcPayRecord createPayRecord(XcOrders orders) {

        XcPayRecord payRecord = new XcPayRecord();
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);//支付记录交易号
        //记录关键订单ID
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");//未支付
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;

    }


    //根据业务id查询订单
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }


}
