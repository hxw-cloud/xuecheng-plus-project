package com.hxw.ucenter.feignclient.factory;


import com.hxw.ucenter.feignclient.CheckCodeClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码失败，熔断异常:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
