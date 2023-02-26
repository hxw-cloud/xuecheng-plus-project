package com.hxw;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.hxw.mapper")
@EnableFeignClients(basePackages = "com.hxw.feignclient")
public class contentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(contentServiceApplication.class, args);
    }
}
