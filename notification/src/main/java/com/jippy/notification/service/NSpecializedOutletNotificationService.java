package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.exception.NotificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NSpecializedOutletNotificationService {

    private final NotificationService
            notificationService;

    @KafkaListener(
            topics = "co-order-events",
            groupId = "specialized-outlet-group"
    )
    @Transactional
    public void consume(
            NOrderEvent event) {

        if (event == null) {

            log.error("Kafka event is null");

            return;
        }

        log.info(
                "Received Kafka event orderId={} outletId={} status={}",
                event.getOrderId(),
                event.getOutletId(),
                event.getStatus()
        );

        try {

            /*
             * SAVE NOTIFICATION
             */
            OrderNotificationStatus status =
                    notificationService
                            .processNotification(
                                    event
                            );

            /*
             * SUBJECT
             */
            String subject;

            if ("REJECTED".equalsIgnoreCase(
                    event.getStatus())) {

                subject =
                        NConstants.SUBJECT_REJECTED_ORDER;

            } else {

                subject =
                        NConstants.SUBJECT_NEW_ORDER;
            }

            /*
             * FETCH TEMPLATE
             */
            Notification notification =
                    notificationService
                            .getNotificationTemplate(
                                    subject
                            );

            /*
             * SEND FCM
             */
            sendFCM(
                    event,
                    notification
            );

            /*
             * UPDATE STATUS
             */
            notificationService
                    .markAsSent(
                            event.getOrderId(),
                            status.getNotificationRecipientId()
                    );

            log.info(
                    "Notification processed successfully outletId={}",
                    event.getOutletId()
            );

        } catch (Exception ex) {

            log.error(
                    "Notification processing failed orderId={} outletId={}",
                    event.getOrderId(),
                    event.getOutletId(),
                    ex
            );
        }
    }

    private void sendFCM(
            NOrderEvent event,
            Notification notification) {

        try {

            String topic =
                    NConstants.TOPIC_PREFIX
                            + event.getOutletId();

            Message message =
                    Message.builder()

                            .setTopic(topic)

                            .setNotification(
                                    com.google.firebase.messaging.Notification
                                            .builder()
                                            .setTitle(
                                                    notification.getSubject()
                                            )
                                            .setBody(
                                                    notification.getMessage()
                                            )
                                            .build()
                            )

                            .putData(
                                    "orderId",
                                    event.getOrderId()
                            )

                            .putData(
                                    "status",
                                    event.getStatus()
                            )

                            .putData(
                                    "areaId",
                                    String.valueOf(
                                            event.getAreaId()
                                    )
                            )

                            .putData(
                                    "rejectedOutletId",
                                    String.valueOf(
                                            event.getRejectedOutletId()
                                    )
                            )

                            .build();

            String response =
                    FirebaseMessaging
                            .getInstance()
                            .send(message);

            log.info(
                    "FCM sent successfully topic={} response={}",
                    topic,
                    response
            );

        } catch (FirebaseMessagingException ex) {

            log.error(
                    "FCM failed outletId={}",
                    event.getOutletId(),
                    ex
            );

            throw new NotificationException(
                    "Failed to send FCM notification"
            );
        }
    }
}