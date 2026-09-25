package com.jippy.notification.consumer;

import com.jippy.notification.dto.CoProfileIncompleteCustomer;
import com.jippy.notification.service.NProfileIncompleteNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NProfileIncompleteKafkaConsumer {

    private final NProfileIncompleteNotificationService notificationService;

    @KafkaListener(
            topics = "profile-incomplete-notification",
            groupId = "profile_incomplete_notification_group",
            containerFactory = "profileIncompleteKafkaListenerContainerFactory"
    )
    public void consume(CoProfileIncompleteCustomer event) {
        if (event != null) {
            log.info("KAFKA_MESSAGE_RECEIVED | ProfileIncomplete | customerId={}", event.getCustomerId());
            notificationService.processNotification(event);
        }
    }
}