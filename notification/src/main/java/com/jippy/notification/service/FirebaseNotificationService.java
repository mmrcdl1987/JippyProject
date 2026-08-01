package com.jippy.notification.service;

public interface FirebaseNotificationService {

    String sendNotification(
            String token,
            String title,
            String body
    );
}
