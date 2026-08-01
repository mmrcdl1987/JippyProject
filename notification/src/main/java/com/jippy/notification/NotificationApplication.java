package com.jippy.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class NotificationApplication {

	public static void main(String[] args) {

        // MUST BE BEFORE SpringApplication.run()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		SpringApplication.run(NotificationApplication.class, args);
	}
}