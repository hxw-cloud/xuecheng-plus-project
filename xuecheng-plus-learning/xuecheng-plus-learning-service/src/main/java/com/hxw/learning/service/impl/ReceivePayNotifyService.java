package com.hxw.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.hxw.learning.config.PayNotifyConfig;
import com.hxw.learning.service.MyCourseTablesService;
import com.hxw.messagesdk.model.po.MqMessage;
import com.hxw.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mr.M
 * @version 1.0
 * @description 接收支付结果通知service
 * @date 2022/10/5 5:06
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MyCourseTablesService myCourseTablesService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //接收支付结果通知
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = PayNotifyConfig.CHOOSECOURSE_PAYNOTIFY_QUEUE),
//            exchange = @Exchange(value = PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, type = ExchangeTypes.FANOUT)
//
//    ))
    //监听支付结果通知队列
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(String message) {
        //获取消息
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //判段该消息是否是自己的消息
        if (PayNotifyConfig.MESSAGE_TYPE.equals(mqMessage.getMessageType()) &&
                "602001".equals(mqMessage.getBusinessKey2())) {
            //只处理支付结果通知的消息,并且是学生购买课程的清单
            //获取选课记录id
            String choosecourseId = mqMessage.getBusinessKey1();

            //添加选课
            boolean b = myCourseTablesService.saveChooseCourseStauts(choosecourseId);
            if (b) {
                //向订单服务回复
                send(mqMessage);
            }
        }
    }

    /**
     * @param message 回复消息
     * @return void
     * @description 回复消息
     * @author Mr.M
     * @date 2022/9/20 9:43
     */
    public void send(MqMessage message) {
        //转json
        String msg = JSON.toJSONString(message);
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE, msg);
        log.debug("学习中心服务向订单服务回复消息:{}", message);
    }


}
