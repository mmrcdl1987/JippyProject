package com.jippy.division;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Division Microservice REST API's",
                description = "Division Microservices that includes creation of coupons,pricing and settlements",
                version = "v1"
        )
)
public class DivisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(DivisionApplication.class, args);
	}

}
