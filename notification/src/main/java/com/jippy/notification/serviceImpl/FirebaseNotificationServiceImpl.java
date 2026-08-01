package com.jippy.notification.serviceImpl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jippy.notification.service.FirebaseNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseNotificationServiceImpl
        implements FirebaseNotificationService {

    @Override
    public String sendNotification(
            String token,
            String title,
            String body) {

        try {

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                    .build();

            String response = FirebaseMessaging
                    .getInstance()
                    .send(message);

            log.info("Firebase notification sent successfully : {}", response);

            return response;

        } catch (Exception ex) {

            log.error("Failed to send Firebase notification", ex);

            throw new RuntimeException(ex);
        }
    }
}
