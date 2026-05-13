package com.jippy.customerandorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
//@EnableFeignClients(basePackages = "com.jippy.customerandorder.feignClient")
@EnableFeignClients
public class CustomerandorderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerandorderApplication.class, args);
	}

}
