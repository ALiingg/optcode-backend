package com.example.optbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableFeignClients(basePackages = { "com.example.optbackend" })
@ComponentScan(basePackages = { "com.example" })
@EnableDiscoveryClient
@EnableWebMvc
public class OptBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OptBackendApplication.class, args);
    }

}
