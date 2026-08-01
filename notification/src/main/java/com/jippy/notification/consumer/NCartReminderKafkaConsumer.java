package com.jippy.notification.consumer;

import com.jippy.notification.dto.NCartReminderDto;
import com.jippy.notification.service.ICartReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NCartReminderKafkaConsumer {

    private final ICartReminderService cartReminderService;

    @KafkaListener(
            topics = "cart-reminder-notification",
            groupId = "notification-group")
    public void consume(NCartReminderDto reminder) {

        log.info("Received Cart Reminder : {}", reminder.getCustomerId());

        cartReminderService.processReminder(reminder);

    }
}
