package com.jippy.notification.consumer;

import com.jippy.notification.dto.NMealReminderDto;
import com.jippy.notification.service.IMealReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NMealReminderKafkaConsumer {

    private final IMealReminderService mealReminderService;

    @KafkaListener(
            topics = "meal-reminder-notification",
            groupId = "notification-group"
    )
    public void consume(NMealReminderDto reminder) {

        log.info(
                "KAFKA_MESSAGE_RECEIVED | CustomerId={} | MealType={}",
                reminder.getCustomerId(),
                reminder.getMealType()
        );

        mealReminderService.processMealReminder(reminder);
    }
}