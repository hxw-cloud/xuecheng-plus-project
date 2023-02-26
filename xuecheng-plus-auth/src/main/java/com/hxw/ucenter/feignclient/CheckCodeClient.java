package com.hxw.ucenter.feignclient;

import com.hxw.ucenter.feignclient.factory.CheckCodeClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "checkcode", url = "127.0.0.1:63075", fallbackFactory = CheckCodeClientFactory.class)
public interface CheckCodeClient {


    @PostMapping(value = "/checkcode/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);
}
