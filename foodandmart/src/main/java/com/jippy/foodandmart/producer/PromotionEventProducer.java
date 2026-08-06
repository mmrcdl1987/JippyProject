package com.jippy.foodandmart.producer;

import com.jippy.foodandmart.constants.KafkaTopics;
import com.jippy.foodandmart.dto.PromotionEvent;
import com.jippy.foodandmart.enums.PromotionEventType;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish Merchant Promotion Created Event
     */
    public void publishPromotionCreated(Integer promotionPlanId) {

        publishEvent(
                PromotionEventType.MERCHANT_PROMOTION_CREATED,
                promotionPlanId);
    }

    /**
     * Publish Merchant Promotion Updated Event
     */
    public void publishPromotionUpdated(Integer promotionPlanId) {

        publishEvent(
                PromotionEventType.MERCHANT_PROMOTION_UPDATED,
                promotionPlanId);
    }

    /**
     * Publish Merchant Promotion Deleted Event
     */
    public void publishPromotionDeleted(Integer promotionPlanId) {

        publishEvent(
                PromotionEventType.MERCHANT_PROMOTION_DELETED,
                promotionPlanId);
    }

    /**
     * Common Publisher
     */
    private void publishEvent(
            PromotionEventType eventType,
            Integer sourceId) {

        PromotionEvent event = PromotionEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .sourceId(sourceId)
                .eventTime(LocalDateTime.now())
                .build();

        kafkaTemplate.send(
                KafkaTopics.PROMOTION_EVENTS,
                sourceId.toString(),
                event);

        log.info(
                "[KAFKA] Promotion event published | eventType={} | sourceId={}",
                eventType,
                sourceId);
    }
}