package com.hxw.orders.service;

import com.hxw.orders.model.dto.AddOrderDto;
import com.hxw.orders.model.dto.PayRecordDto;
import com.hxw.orders.model.dto.PayStatusDto;
import com.hxw.orders.model.po.XcPayRecord;

/**
 * 订单服务接口
 */
public interface OrderService {


    /**
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @description 创建商品订单
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 查询支付交易记录
     */
    public XcPayRecord getPayRecordByPayno(String payNo);


    /**
     * @param payStatusDto 支付结果信息
     * @return void
     * @description 保存支付宝支付结果
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);
}
