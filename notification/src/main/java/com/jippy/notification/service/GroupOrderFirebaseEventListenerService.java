package com.jippy.notification.service;

import com.jippy.notification.dto.GroupOrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupOrderFirebaseEventListenerService {

    private final FirebaseNotificationService firebaseNotificationService;

    @KafkaListener(
            topics = "group-order-events",
            groupId = "group_order_fcm_consumers",
            containerFactory = "groupOrderKafkaListenerContainerFactory"
    )
    public void listenAndPushToFirebase(GroupOrderEventDto event) {
        if (event == null) {
            return;
        }

        log.info("KAFKA_MESSAGE_RECEIVED | GroupOrderEvent | type={}", event.getEventType());

        String firebaseTopic = "group_order_" + event.getGroupOrdersInvitationId();

        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("eventType", String.valueOf(event.getEventType()));
        dataPayload.put("groupOrdersInvitationId", String.valueOf(event.getGroupOrdersInvitationId()));
        dataPayload.put("customerId", String.valueOf(event.getCustomerId()));
        dataPayload.put("customerName", String.valueOf(event.getCustomerName()));
        dataPayload.put("deliveryAddressId", String.valueOf(event.getDeliveryAddressId()));

        try {
            String response = firebaseNotificationService.sendTopicDataMessage(firebaseTopic, dataPayload);
            log.info("FCM_GROUP_TOPIC_DATA_DISPATCH_SUCCESS | topic={} | messageId={}", firebaseTopic, response);
        } catch (Exception e) {
            log.error("FCM_GROUP_TOPIC_DATA_DISPATCH_FAILED | topic={}", firebaseTopic, e);
        }
    }
}
