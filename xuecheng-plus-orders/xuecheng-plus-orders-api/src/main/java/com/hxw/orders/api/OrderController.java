package com.hxw.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.hxw.base.exception.XueChengPlusException;
import com.hxw.orders.config.AlipayConfig;
import com.hxw.orders.model.dto.AddOrderDto;
import com.hxw.orders.model.dto.PayRecordDto;
import com.hxw.orders.model.dto.PayStatusDto;
import com.hxw.orders.model.po.XcPayRecord;
import com.hxw.orders.service.OrderService;
import com.hxw.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {


    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Autowired
    private OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String id = user.getId();
        return orderService.createOrder(id, addOrderDto);
    }


    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestPay(String payNo, HttpServletResponse httpResponse)
            throws IOException, AlipayApiException {


        //校验payNo交易号是否存在
        XcPayRecord payRecord = orderService.getPayRecordByPayno(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("找不到商品下单记录");
        }

        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL,
                APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET,
                ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);//获得初始化的AlipayClient


        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request


        //告诉支付宝字符包支付通知的地址
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://tjxt-user-t.itheima.net/xuecheng/orders/receivenotify");
        //在公共参数中设置回跳和通知地址


        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + payNo + "\"," +
                "    \"total_amount\":" + payRecord.getTotalPrice() + "," +
                "    \"subject\":\"" + payRecord.getOrderName() + "\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数


        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();


    }

    @ApiOperation("接收支付结果通知")
    @PostMapping("/receivenotify")
    public void receivenotify(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, AlipayApiException {

        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = httpRequest.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//

        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
        //计算得出通知验证结果
        //boolean AlipaySignature.rsaCheckV1(
        //                  Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean verify_result = AlipaySignature
                .rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if (verify_result) {//验证成功

            //////////////////////////////////////////////////////////////////////////////////////////
            //请在这里加上商户的业务逻辑程序代码

            //商户订单号

            String out_trade_no = new String(httpRequest.getParameter("out_trade_no")
                    .getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号

            String trade_no = new String(httpRequest.getParameter("trade_no")
                    .getBytes("ISO-8859-1"), "UTF-8");

            //交易状态
            String trade_status = new String(httpRequest.getParameter("trade_status")
                    .getBytes("ISO-8859-1"), "UTF-8");


            //app_id
            String app_id = new String(httpRequest.getParameter("app_id")
                    .getBytes("ISO-8859-1"), "UTF-8");


            //支付的总金额
            String total_amount = new String(httpRequest.getParameter("total_amount")
                    .getBytes("ISO-8859-1"), "UTF-8");

            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

            if (trade_status.equals("TRADE_SUCCESS")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序
                //更新订单表状态
                PayStatusDto payStatusDto = new PayStatusDto();
                //支付宝通知我们的appid
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_status(trade_status);//交易结果
                payStatusDto.setOut_trade_no(out_trade_no);//商户订单号
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);//支付的总金额

                //支付宝渠道编号603002
                orderService.saveAliPayStatus(payStatusDto);
                System.out.println("==================支付成功===========================");
                //注意：
                //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }

            //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
            httpResponse.getWriter().println("success");
            //请不要修改或删除

            //////////////////////////////////////////////////////////////////////////////////////////
        } else {//验证失败
            httpResponse.getWriter().println("fail");
        }
    }


}
