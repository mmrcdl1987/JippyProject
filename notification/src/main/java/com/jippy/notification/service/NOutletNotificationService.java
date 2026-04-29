package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NOutletNotificationService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "new-orders", groupId = "outlet-group")
    @Transactional
    public void consume(NOrderEvent event) {

        // Null validation
        if (event == null) {
            log.error("Order event is null");
            return;
        }

        if (event.getOrderId() == null || event.getOutletId() == null) {
            log.error("Invalid event data | orderId={}, outletId={}", event.getOrderId(), event.getOutletId());
            return;
        }

        log.info("Start processing order event | orderId={}, outletId={}", event.getOrderId(), event.getOutletId());

        try {
            // 1. Save Notification
            Notification notification = notificationService.processNotification(event);

            // 2. Send FCM
            sendFCM(event, notification);

            // 3. Mark as sent
            notificationService.markAsSent(event.getOrderId(), notification.getNotificationId());

            log.info("Order event processed successfully | orderId={}", event.getOrderId());

        } catch (NotificationException e) {
            log.error("Notification processing failed | orderId={}, outletId={}, error={}", 
                    event.getOrderId(), event.getOutletId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing order event | orderId={}, outletId={}, error={}",
                    event.getOrderId(), event.getOutletId(), e.getMessage(), e);
        }
    }

    private void sendFCM(NOrderEvent event, Notification notification) {

        // Null validation
        if (NConstants.TOPIC_PREFIX == null) {
            log.error("Topic prefix configuration is missing");
            throw new NotificationException("Firebase topic configuration missing");
        }

        String topic = NConstants.TOPIC_PREFIX + event.getOutletId();

        log.info("Sending FCM notification | orderId={}, topic={}", event.getOrderId(), topic);

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getSubject())
                        .setBody(notification.getMessage())
                        .build())
                .putData("orderId", String.valueOf(event.getOrderId()))
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);

            log.info("FCM sent successfully | orderId={}, topic={}", event.getOrderId(), topic);

        } catch (FirebaseMessagingException e) {
            log.error("FCM failed | orderId={}, topic={}, error={}", event.getOrderId(), topic, e.getMessage(), e);
            throw new NotificationException("Failed to send FCM notification");
        }
    }
}