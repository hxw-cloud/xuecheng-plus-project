package com.hxw;


import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients("com.hxw.feignclient")
public class contentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(contentApiApplication.class, args);
    }
}
