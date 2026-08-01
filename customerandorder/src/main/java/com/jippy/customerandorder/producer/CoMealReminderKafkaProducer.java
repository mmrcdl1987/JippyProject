package com.jippy.customerandorder.producer;

import com.jippy.customerandorder.dto.CoMealReminderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoMealReminderKafkaProducer {

    private static final String TOPIC = "meal-reminder-notification";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMealReminder(CoMealReminderDto reminder) {

        log.info("Publishing Meal Reminder | CustomerId={} | MealType={}",
                reminder.getCustomerId(),
                reminder.getMealType());

        kafkaTemplate.send(TOPIC, reminder);

        log.info("Meal Reminder Published Successfully");
    }
}