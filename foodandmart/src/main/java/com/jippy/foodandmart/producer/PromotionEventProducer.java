package com.jippy.foodandmart.producer;

import com.jippy.foodandmart.constants.KafkaTopics;
import com.jippy.foodandmart.dto.PromotionEvent;
import com.jippy.foodandmart.enums.PromotionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPromotionCreated(Integer promotionPlanId) {

        publishEvent(PromotionEventType.MERCHANT_PROMOTION_CREATED, promotionPlanId);
    }

    public void publishPromotionUpdated(Integer promotionPlanId) {

        publishEvent(PromotionEventType.MERCHANT_PROMOTION_UPDATED, promotionPlanId);
    }

    public void publishPromotionDeleted(Integer promotionPlanId) {

        publishEvent(PromotionEventType.MERCHANT_PROMOTION_DELETED, promotionPlanId);
    }

    private void publishEvent(PromotionEventType eventType, Integer sourceId) {

        if (eventType == null) {
            throw new IllegalArgumentException("Promotion event type must not be null");
        }

        if (sourceId == null) {
            throw new IllegalArgumentException("Promotion source ID must not be null");
        }

        PromotionEvent event = PromotionEvent.builder().eventId(UUID.randomUUID()).eventType(eventType).sourceId(sourceId).eventTime(LocalDateTime.now()).build();

        String topic = KafkaTopics.PROMOTION_EVENTS;
        String messageKey = sourceId.toString();

        try {

            kafkaTemplate.send(topic, messageKey, event).whenComplete((result, exception) -> {

                if (exception != null) {

                    log.error("[KAFKA] Promotion event publishing failed | " + "eventId={} | eventType={} | " + "sourceId={} | topic={}", event.getEventId(), eventType, sourceId, topic, exception);

                    return;
                }

                if (result == null) {

                    log.error("[KAFKA] Kafka returned null send result | " + "eventId={} | sourceId={}", event.getEventId(), sourceId);

                    return;
                }

                var metadata = result.getRecordMetadata();

                log.info("[KAFKA] Promotion event published successfully | " + "eventId={} | eventType={} | sourceId={} | " + "topic={} | partition={} | offset={}", event.getEventId(), eventType, sourceId, metadata.topic(), metadata.partition(), metadata.offset());
            });

        } catch (Exception exception) {

            log.error("[KAFKA] Failed to initiate promotion event publishing | " + "eventType={} | sourceId={} | topic={}", eventType, sourceId, topic, exception);

            throw exception;
        }
    }
}