package com.jippy.driver.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @KafkaListener(topics = "accepted-orders", groupId = "driver-service-group-v3")
    public void handleOrderAccepted(String eventJson) {

        log.info("consumer called ===========================");
        log.info("Received payload: {}", eventJson);

        // Use ObjectMapper to convert manually
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Needed for LocalDateTime parsing

        try {
            OrderAcceptedEvent orderAcceptedEvent = objectMapper.readValue(eventJson, OrderAcceptedEvent.class);

            long targetEpochMillis;

            // Parse deliveryRequestAt to Epoch Milliseconds (Asia/Kolkata timezone)
            if (orderAcceptedEvent.getDeliveryRequestAt() != null) {
                targetEpochMillis = orderAcceptedEvent.getDeliveryRequestAt()
                        .atZone(ZoneId.of("Asia/Kolkata"))
                        .toInstant()
                        .toEpochMilli();
                log.info("Scheduled delivery dispatch for order {} at epoch millis: {}", orderAcceptedEvent.getOrderId(), targetEpochMillis);
            } else {
                targetEpochMillis = System.currentTimeMillis(); // Fallback to immediate dispatch
            }
            redisTemplate.opsForZSet().add(DISPATCH_QUEUE_KEY, orderAcceptedEvent.getOrderId(), (double) targetEpochMillis);
        } catch (Exception e) {
            log.error("Failed to deserialize event payload", e);
        }
    }

}
