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
            groupId = "profile_incomplete_notification_group"
    )
    public void consume(CoProfileIncompleteCustomer event) {

        log.info("PROFILE_INCOMPLETE_NOTIFICATION_EVENT_RECEIVED : {}", event);

        notificationService.processNotification(event);
    }
}