package com.jippy.customerandorder.producer;

import com.jippy.customerandorder.dto.CoProfileIncompleteCustomer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoProfileIncompleteKafkaProducer {

    private static final String TOPIC = "profile-incomplete-notification";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(CoProfileIncompleteCustomer customer) {

        kafkaTemplate.send(TOPIC, customer);

        log.info("Profile Incomplete Event Sent for Customer Id: {}",
                customer.getCustomerId());
    }
}