package com.jippy.customerandorder.producer;

import com.jippy.customerandorder.dto.CoCartReminderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoCartReminderKafkaProducer {

    private static final String TOPIC = "cart-reminder-notification";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCartReminder(CoCartReminderDto reminder) {

        log.info("Publishing Cart Reminder for Customer : {}",
                reminder.getCustomerId());

        kafkaTemplate.send(TOPIC, reminder);

        log.info("Cart Reminder Published Successfully");
    }
}