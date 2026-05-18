package com.jippy.foodandmart;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // ✅ ADD
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Food and Mart Microservice REST API's",
                description = "Food and Mart Microservices includes creation of merchants,outlets,products",
                version = "v1"
        )
)
public class FoodAndmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodAndmartApplication.class, args);
    }
}