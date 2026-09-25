package com.jippy.notification.service;

import java.util.Map;

public interface FirebaseNotificationService {

    String sendNotification(
            String token,
            String title,
            String body
    );

    String sendNotification(
            String token,
            String title,
            String body,
            Map<String, String> dataPayload
    );

    String sendTopicNotification(
            String topic,
            String title,
            String body,
            Map<String, String> dataPayload
    );

    String sendTopicDataMessage(
            String topic,
            Map<String, String> dataPayload
    );
}
