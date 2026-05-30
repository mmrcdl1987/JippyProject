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
public class NSpecializedOutletNotificationService {

    private final NotificationService notificationService;

    @KafkaListener(topics = "co-order-events", groupId = "specialized-outlet-group")
    @Transactional
    public void consume(NOrderEvent event) {

        log.info("KAFKA_START | SPECIALIZED_OUTLET_NOTIFICATION | orderId={} | outletId={}", event != null ? event.getOrderId() : null, event != null ? event.getOutletId() : null);

        try {

            /*
             * EVENT VALIDATION
             */
        if (event == null) {

                log.error("VALIDATION_FAILED | EVENT_NULL");

                return;
            }

            if (event.getOrderId() == null || event.getOutletId() == null) {

                log.error("VALIDATION_FAILED | ORDER_ID_OR_OUTLET_ID_MISSING | orderId={} | outletId={}", event.getOrderId(), event.getOutletId());

            return;
        }

            if (event.getOutletId() <= 0) {

                log.error("VALIDATION_FAILED | INVALID_OUTLET_ID | outletId={}", event.getOutletId());

                return;
            }

            if (event.getNotificationType() == null && event.getStatus() == null) {

                log.error("VALIDATION_FAILED | NOTIFICATION_TYPE_AND_STATUS_MISSING");

                return;
            }

            /*
             * SAVE NOTIFICATION STATUS
             */
            OrderNotificationStatus status = notificationService.processNotification(event);

            /*
             * FETCH TEMPLATE
             */
            String subject = resolveSubject(event);

            Notification notification = notificationService.getNotificationTemplate(subject);

            /*
             * SEND FCM
             */
            sendFCM(event, notification);

            /*
             * MARK SENT
             */
            notificationService.markAsSent(event.getOrderId(), status.getNotificationRecipientId());

            log.info("KAFKA_END | SPECIALIZED_OUTLET_NOTIFICATION_SUCCESS | orderId={}", event.getOrderId());

        } catch (NotificationException ex) {

            log.error("KAFKA_ERROR | SPECIALIZED_OUTLET_NOTIFICATION_FAILED | orderId={} | error={}", event != null ? event.getOrderId() : null, ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("KAFKA_ERROR | UNEXPECTED_ERROR | orderId={} | error={}", event != null ? event.getOrderId() : null, ex.getMessage(), ex);

            throw new NotificationException("Notification processing failed");
        }
        }

    private String resolveSubject(NOrderEvent event) {

        if (event.getNotificationType() != null && !event.getNotificationType().isBlank()) {

            return event.getNotificationType();
        }

        if ("REJECTED".equalsIgnoreCase(event.getStatus())) {

            return "REJECTED_ORDER";
        }

        return "ORDER_CREATED";
    }

    private void sendFCM(NOrderEvent event, Notification notification) {

        if (NConstants.TOPIC_PREFIX == null || NConstants.TOPIC_PREFIX.isBlank()) {

            log.error("VALIDATION_FAILED | TOPIC_PREFIX_MISSING");

            throw new NotificationException("Firebase topic configuration missing");
        }

        String topic = NConstants.TOPIC_PREFIX + event.getOutletId().toString().trim();

        log.info("FCM_START | SEND_SPECIALIZED_OUTLET_NOTIFICATION | topic={} | orderId={}", topic, event.getOrderId());

        try {

            Message.Builder builder = Message.builder()

                    .setTopic(topic)

                    .setNotification(com.google.firebase.messaging.Notification.builder().setTitle(notification.getSubject()).setBody(notification.getMessage()).build())

                    .putData("orderId", String.valueOf(event.getOrderId()))

                    .putData("status", String.valueOf(event.getStatus()))

                    .putData("areaId", String.valueOf(event.getAreaId()))

                    .putData("rejectedOutletId", String.valueOf(event.getRejectedOutletId()))

                    .putData("notificationType", String.valueOf(event.getNotificationType()))

                    .putData("orderType", String.valueOf(event.getOrderType()));


            if (event.getScheduledDeliveryDateTime() != null) {

                builder.putData("scheduledDeliveryDateTime", event.getScheduledDeliveryDateTime().toString());
            }

            if (event.getMealSubscriptionId() != null) {

                builder.putData("mealSubscriptionId", String.valueOf(event.getMealSubscriptionId()));
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());

            log.info("FCM_END | SEND_SPECIALIZED_OUTLET_NOTIFICATION_SUCCESS | response={}", response);

        } catch (FirebaseMessagingException ex) {

            log.error("FCM_ERROR | SEND_SPECIALIZED_OUTLET_NOTIFICATION_FAILED | orderId={} | error={}", event.getOrderId(), ex.getMessage(), ex);

            throw new NotificationException("Failed to send FCM notification");
        }
    }
}