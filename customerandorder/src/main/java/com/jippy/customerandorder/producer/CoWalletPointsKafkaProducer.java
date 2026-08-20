package com.jippy.customerandorder.producer;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoWalletPointsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoWalletPointsKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendWalletPointsEvent(CoWalletPointsEvent event) {

        log.info(
                "Publishing Wallet Points Event | orderId={} | customerId={} | points={}",
                event.getOrderId(),
                event.getCustomerId(),
                event.getTransactionPoints()
        );

//        this sends the event to the WALLET_POINTS_TOPIC
//        with the orderId as the key and the event as the value
        kafkaTemplate.send(
                COConstants.WALLET_POINTS_TOPIC,
                event.getOrderId(),
                event
        );

        log.info(
                "Wallet Points Event Published Successfully | orderId={}",
                event.getOrderId()
        );
    }
}