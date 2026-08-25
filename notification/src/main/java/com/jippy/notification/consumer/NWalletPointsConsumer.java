package com.jippy.notification.consumer;

import com.jippy.notification.dto.NWalletPointsEvent;
import com.jippy.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NWalletPointsConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "wallet-points-earned",
            groupId = "notification-wallet-points-group",
            containerFactory = "walletPointsKafkaListenerContainerFactory"

    )
    public void consumeWalletPointsEvent(NWalletPointsEvent event) {

        log.info(
                "WALLET_POINTS_EVENT_RECEIVED | orderId={} | " +
                        "customerId={} | points={}",
                event.getOrderId(),
                event.getCustomerId(),
                event.getTransactionPoints()
        );

        notificationService.processWalletPointsNotification(
                event
        );
    }
}
