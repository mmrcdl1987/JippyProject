package com.jippy.notification;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;


@SpringBootApplication
public class NotificationApplication {


	public static void main(String[] args) {

        // MUST BE BEFORE SpringApplication.run()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(NotificationApplication.class, args);
	}

}
