package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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

        double targetEpochMillis = (double) event.getDeliveryRequestAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        redisTemplate.opsForZSet().add(DISPATCH_QUEUE_KEY, event.getOrderId(), targetEpochMillis);

        log.info("Scheduled delivery dispatch for order {} at {}", event.getOrderId(), event.getDeliveryRequestAt());

        //ack.acknowledge();
    }

}
