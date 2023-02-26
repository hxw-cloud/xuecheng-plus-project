package com.hxw.learning;

import com.hxw.learning.service.impl.ReceivePayNotifyService;
import com.hxw.messagesdk.model.po.MqMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/20 20:27
 */
@SpringBootTest
public class PayNotifyServiceTest {

    @Autowired
    ReceivePayNotifyService receivePayNotifyService;

    @Test
    public void testSend() {
        MqMessage mqMessage = new MqMessage();
        mqMessage.setId(100L);
        receivePayNotifyService.send(mqMessage);
    }

}
