package com.jippy.division.consumer;

import com.jippy.division.constants.KafkaTopics;
import com.jippy.division.dto.PromotionEvent;
import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionEventConsumer {

    private final PromotionScheduleService promotionScheduleService;

    @KafkaListener(
            topics = KafkaTopics.PROMOTION_EVENTS,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePromotionEvent(PromotionEvent event) {

        log.info(
                "[KAFKA] Promotion event received | eventType={} | sourceId={}",
                event.getEventType(),
                event.getSourceId());

        try {

            switch (event.getEventType()) {

                case MERCHANT_PROMOTION_CREATED ->

                        promotionScheduleService
                                .createMerchantPromotionSchedule(
                                        event.getSourceId());

                case MERCHANT_PROMOTION_UPDATED ->

                        promotionScheduleService
                                .updateMerchantPromotionSchedule(
                                        event.getSourceId());

                case MERCHANT_PROMOTION_DELETED ->

                        promotionScheduleService
                                .deleteMerchantPromotionSchedule(
                                        event.getSourceId());

                default ->

                        log.warn(
                                "[KAFKA] Unsupported promotion event | eventType={}",
                                event.getEventType());
            }

            log.info(
                    "[KAFKA] Promotion event processed successfully | eventType={} | sourceId={}",
                    event.getEventType(),
                    event.getSourceId());

        } catch (Exception ex) {

            log.error(
                    "[KAFKA] Failed to process promotion event | eventType={} | sourceId={}",
                    event.getEventType(),
                    event.getSourceId(),
                    ex);

            throw ex;
        }
    }
}