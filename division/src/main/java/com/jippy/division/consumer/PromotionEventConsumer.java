package com.jippy.division.consumer;

import com.jippy.division.constants.KafkaTopics;
import com.jippy.division.dto.PromotionEvent;
import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionEventConsumer {

    private final PromotionScheduleService promotionScheduleService;

    @KafkaListener(topics = KafkaTopics.PROMOTION_EVENTS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumePromotionEvent(PromotionEvent event, ConsumerRecord<String, PromotionEvent> record) {

        log.info("[KAFKA] MESSAGE RECEIVED | " + "topic={} | partition={} | offset={} | " + "eventId={} | eventType={} | sourceId={}",

                record.topic(), record.partition(), record.offset(),

                event != null ? event.getEventId() : null, event != null ? event.getEventType() : null, event != null ? event.getSourceId() : null);

        if (event == null || event.getEventType() == null) {

            log.error("[KAFKA] Invalid promotion event | " + "topic={} | partition={} | offset={}",

                    record.topic(), record.partition(), record.offset());

            return;
        }

        try {

            switch (event.getEventType()) {

                case MERCHANT_PROMOTION_CREATED -> {

                    log.info("[KAFKA] Creating promotion schedule | sourceId={}", event.getSourceId());

                    promotionScheduleService.createMerchantPromotionSchedule(event.getSourceId());
                }

                case MERCHANT_PROMOTION_UPDATED -> {

                    log.info("[KAFKA] Updating promotion schedule | sourceId={}", event.getSourceId());

                    promotionScheduleService.updateMerchantPromotionSchedule(event.getSourceId());
                }

                case MERCHANT_PROMOTION_DELETED -> {

                    log.info("[KAFKA] Deleting promotion schedule | sourceId={}", event.getSourceId());

                    promotionScheduleService.deleteMerchantPromotionSchedule(event.getSourceId());
                }

                default -> log.warn("[KAFKA] Unsupported event type | eventType={}", event.getEventType());
            }

            log.info("[KAFKA] EVENT PROCESSED SUCCESSFULLY | " + "eventId={} | eventType={} | sourceId={}",

                    event.getEventId(), event.getEventType(), event.getSourceId());

        } catch (Exception exception) {

            log.error("[KAFKA] EVENT PROCESSING FAILED | " + "eventId={} | eventType={} | sourceId={}",

                    event.getEventId(), event.getEventType(), event.getSourceId(), exception);

            throw exception;
        }
    }
}