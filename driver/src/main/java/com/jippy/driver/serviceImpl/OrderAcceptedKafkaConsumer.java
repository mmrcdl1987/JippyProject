package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAcceptedKafkaConsumer {
    // Declare public constant so DeliveryDispatchPoller can reference it
    public static final String DISPATCH_QUEUE_KEY = "delivery:dispatch:queue";

    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "accepted-orders", groupId = "driver-service-group")
    public void handleOrderAccepted(OrderAcceptedEvent event) {

        log.info("consumer called ===========================");

        long targetEpochMillis;

        // FIX: Pull directly from 'event'
        if (event.getDeliveryRequestAt() != null) {
//            targetEpochMillis = event.getDeliveryRequestAt()
//                    .atZone(ZoneId.of("Asia/Kolkata"))
//                    .toInstant()
//                    .toEpochMilli();
            targetEpochMillis = System.currentTimeMillis();

            log.info("===================================targetEpochMillis" + targetEpochMillis);
        } else {
            targetEpochMillis = System.currentTimeMillis();
        }

        redisTemplate.opsForZSet().add("delivery:dispatch:queue", event.getOrderId(), (double) targetEpochMillis);
    }

}
