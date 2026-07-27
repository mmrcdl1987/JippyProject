package com.jippy.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NotificationApplication {

	public static void main(String[] args) {

//		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
//
//		MetricsProperties.System.out.println("JVM TimeZone : " + TimeZone.getDefault().getID());

		SpringApplication.run(NotificationApplication.class, args);
	}
}