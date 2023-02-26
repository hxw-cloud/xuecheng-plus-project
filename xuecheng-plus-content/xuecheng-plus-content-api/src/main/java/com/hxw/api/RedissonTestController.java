package com.hxw.api;

import io.lettuce.core.RedisClient;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

@RestController
@RequestMapping("/redisson")
public class RedissonTestController {

    @Autowired
    private RedissonClient redissonClient;


    /**
     * 入队
     */
    @GetMapping("/joinqueue")public Queue<String> joinqueue(String queuer) {
        RQueue<String> queue = redissonClient.getQueue("queue001");
        queue.add(queuer);
        return queue;
    }


    /**
     * 出队
     */

    @GetMapping("/removequeue")
    public String removequeue() {
        RQueue<String> queue = redissonClient.getQueue("queue001");
        String remove = queue.remove();
        return remove;
    }


}
