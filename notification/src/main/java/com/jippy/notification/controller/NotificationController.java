package com.jippy.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @GetMapping("/send")
    public String sendNotification() {
        // Placeholder for sending notification logic
        return "Notification sent successfully!";
    }
}
