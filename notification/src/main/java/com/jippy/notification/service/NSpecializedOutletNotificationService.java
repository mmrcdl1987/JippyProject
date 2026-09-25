package com.jippy.notification.service;

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

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NSpecializedOutletNotificationService {

    private final NotificationService notificationService;
    private final FirebaseNotificationService firebaseNotificationService;

    @KafkaListener(
            topics = "co-order-events",
            groupId = "specialized-outlet-group",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(NOrderEvent event) {
        log.info("KAFKA_START | SPECIALIZED_OUTLET_NOTIFICATION | orderId={} | outletId={}",
                event != null ? event.getOrderId() : null, event != null ? event.getOutletId() : null);

        try {
            if (event == null || event.getOrderId() == null || event.getOutletId() == null || event.getOutletId() <= 0) {
                log.error("VALIDATION_FAILED | SPECIALIZED_OUTLET_EVENT_INVALID");
                return;
            }

            OrderNotificationStatus status = notificationService.processNotification(event);
            String subject = resolveSubject(event);
            Notification notification = notificationService.getNotificationTemplate(subject);

            String messageId = sendFCM(event, notification);

            if (status != null && status.getOrderNotificationStatusId() != null) {
                notificationService.markAsSent(status.getOrderNotificationStatusId(), messageId);
            }

            log.info("KAFKA_END | SPECIALIZED_OUTLET_NOTIFICATION_SUCCESS | orderId={}", event.getOrderId());

        } catch (NotificationException ex) {
            log.error("KAFKA_ERROR | SPECIALIZED_OUTLET_NOTIFICATION_FAILED | orderId={} | error={}",
                    event != null ? event.getOrderId() : null, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("KAFKA_ERROR | UNEXPECTED_ERROR | orderId={} | error={}",
                    event != null ? event.getOrderId() : null, ex.getMessage(), ex);
            throw new NotificationException("Notification processing failed", ex);
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

    private String sendFCM(NOrderEvent event, Notification notification) {
        String topic = NConstants.TOPIC_PREFIX + event.getOutletId().toString().trim();
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("orderId", String.valueOf(event.getOrderId()));
        dataPayload.put("status", String.valueOf(event.getStatus()));
        dataPayload.put("areaId", String.valueOf(event.getAreaId()));
        dataPayload.put("rejectedOutletId", String.valueOf(event.getRejectedOutletId()));
        dataPayload.put("notificationType", String.valueOf(event.getNotificationType()));
        dataPayload.put("orderType", String.valueOf(event.getOrderType()));

        if (event.getScheduledDeliveryDateTime() != null) {
            dataPayload.put("scheduledDeliveryDateTime", event.getScheduledDeliveryDateTime().toString());
        }
        if (event.getMealSubscriptionId() != null) {
            dataPayload.put("mealSubscriptionId", String.valueOf(event.getMealSubscriptionId()));
        }

        return firebaseNotificationService.sendTopicNotification(
                topic, notification.getSubject(), notification.getMessage(), dataPayload);
    }
}