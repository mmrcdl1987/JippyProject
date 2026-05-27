package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
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

        /*
         * NULL VALIDATION
         */
        if (event == null) {

            log.error("Order event is null");

            return;
        }

        /*
         * FIELD VALIDATION
         */
        if (event.getOrderId() == null || event.getOutletId() == null) {

            log.error("Invalid event data | orderId={}, outletId={}", event.getOrderId(), event.getOutletId());

            return;
        }

        log.info("Start processing order event | orderId={}, outletId={}", event.getOrderId(), event.getOutletId());

        try {

            /*
             * SAVE NOTIFICATION
             */
            OrderNotificationStatus status = notificationService.processNotification(event);

            /*
             * SUBJECT
             */
            String subject;

            if ("REJECTED".equalsIgnoreCase(event.getStatus())) {

                subject = NConstants.SUBJECT_REJECTED_ORDER;

            } else {

                subject = NConstants.SUBJECT_NEW_ORDER;
            }

            /*
             * FETCH TEMPLATE
             */
            Notification notification = notificationService.getNotificationTemplate(subject);

            /*
             * SEND FCM
             */
            sendFCM(event, notification);

            /*
             * UPDATE STATUS
             */
            notificationService.markAsSent(event.getOrderId(), status.getNotificationRecipientId());

            log.info("Order event processed successfully | orderId={}", event.getOrderId());

        } catch (NotificationException e) {

            log.error("Notification processing failed | orderId={}, outletId={}, error={}", event.getOrderId(), event.getOutletId(), e.getMessage(), e);

        } catch (Exception e) {

            log.error("Unexpected error processing order event | orderId={}, outletId={}, error={}", event.getOrderId(), event.getOutletId(), e.getMessage(), e);
        }
    }

    private void sendFCM(NOrderEvent event, Notification notification) {

        if (NConstants.TOPIC_PREFIX == null) {

            log.error("Topic prefix configuration is missing");

            throw new NotificationException("Firebase topic configuration missing");
        }

        //Firebase has strict rules for topic names: they can only contain letters, numbers, and dashes/underscores ([a-zA-Z0-9-_~]+).

        String topic = NConstants.TOPIC_PREFIX + event.getOutletId();

        log.info("Sending FCM notification | orderId={}, topic={}", event.getOrderId(), topic);

        try {

            Message message = Message.builder()

                    .setTopic(topic)

                    .setNotification(com.google.firebase.messaging.Notification.builder().setTitle(notification.getSubject()).setBody(notification.getMessage()).build())

                    .putData("orderId", String.valueOf(event.getOrderId()))

                    .putData("status", String.valueOf(event.getStatus()))

                    .build();

            FirebaseMessaging.getInstance().send(message);

            log.info("FCM sent successfully | orderId={}, topic={}", event.getOrderId(), topic);

        } catch (FirebaseMessagingException e) {

            log.error("FCM failed | orderId={}, topic={}, error={}", event.getOrderId(), topic, e.getMessage(), e);

            throw new NotificationException("Failed to send FCM notification");
        }
    }
}