package com.supwork.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SupworkApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupworkApiGatewayApplication.class, args);
    }

}

