package com.hxw.orders.jobhandler;

import com.alibaba.fastjson.JSON;
import com.hxw.messagesdk.model.po.MqMessage;
import com.hxw.messagesdk.service.MessageProcessAbstract;
import com.hxw.messagesdk.service.MqMessageService;
import com.hxw.orders.config.PayNotifyConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayNotifyTask extends MessageProcessAbstract {


    //支付结果通知消息类型
    public static final String MESSAGE_TYPE = "payresult_notify";
    @Autowired
    private MqMessageService mqMessageService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //任务调度入口
    @XxlJob("NotifyPayResultJobHandler")
    public void notifyPayResultJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        process(shardIndex, shardTotal, MESSAGE_TYPE, 50, 60);
    }


    //执行任务的具体方法
    @Override
    public boolean execute(MqMessage mqMessage) {

        log.debug("开始进行支付结果通知:{}", mqMessage.toString());
        //发布消息

        send(mqMessage);

        return false;
    }

    //接收支付结果通知
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE)
    public void receive(String message) {
        //获取消息
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //获取选课记录id
        Long id = mqMessage.getId();
        //完成消息发送，删除消息
        int completed = mqMessageService.completed(id);
    }


    /**
     * @param message 消息内容
     * @return void
     * @description 发送消息
     * @author Mr.M
     * @date 2022/9/20 9:43
     */
    public void send(MqMessage message) {

        //要发送的信息
        String jsonString = JSON.toJSONString(message);


        //开始发送的信息
        //使用fanout交换机，通过广播模式发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", jsonString);

        log.debug("向支付中心发送结果通知消息完成:{}", message.toString());

    }
}