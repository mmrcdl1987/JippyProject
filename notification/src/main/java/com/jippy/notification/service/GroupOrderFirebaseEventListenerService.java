package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.jippy.notification.dto.GroupOrderEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GroupOrderFirebaseEventListenerService {

    @KafkaListener(topics = "group-order-events", groupId = "group_order_fcm_consumers")
    public void listenAndPushToFirebase(GroupOrderEventDto event) {
        log.info("Processing Kafka event {} for Firebase dispatch", event.getEventType());

        // We use a Firebase Topic name unique to this group room session
        // e.g., "group_order_9842"
        String firebaseTopic = "group_order_" + event.getGroupOrdersInvitationId();

        // 💡 CRITICAL: Use .putData() instead of .setNotification()
        // This ensures the message is treated as a silent "Data Message"
        // that your frontend code can intercept in the background/foreground without displaying a generic system banner.
        Message message = Message.builder()
                .setTopic(firebaseTopic)
                .putData("eventType", event.getEventType())
                .putData("groupOrdersInvitationId", String.valueOf(event.getGroupOrdersInvitationId()))
                .putData("customerId", String.valueOf(event.getCustomerId()))
                .putData("customerName", event.getCustomerName())
                .putData("deliveryAddressId", String.valueOf(event.getDeliveryAddressId()))
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM data payload to topic {}. Response ID: {}", firebaseTopic, response);
        } catch (Exception e) {
            log.error("Failed to dispatch FCM message to group topic", e);
        }
    }
}
