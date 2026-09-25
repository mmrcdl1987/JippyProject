package com.jippy.notification.consumer;

import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NOrderKafkaConsumer {

    private final NotificationService notificationService;
    @KafkaListener(
            topics = {
                    "order-events",
                    "co-order-events",
                    "new-orders",
                    "accepted-orders"
            },
            groupId = "notification-order-group",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void consume(NOrderEvent event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("INVALID_ORDER_EVENT_RECEIVED");
            return;
        }

        log.info("ORDER_EVENT_RECEIVED | orderId={} | type={} | customerId={} | outletId={}",
                event.getOrderId(), event.getNotificationType(), event.getCustomerId(), event.getOutletId());

        notificationService.processOrderNotification(event);
    }
}
