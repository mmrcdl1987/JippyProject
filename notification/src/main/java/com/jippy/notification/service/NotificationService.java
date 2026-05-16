package com.jippy.notification.service;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.exception.NotificationException;
import com.jippy.notification.repository.NotificationRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final OrderNotificationStatusRepository statusRepository;

    @Transactional
    public OrderNotificationStatus processNotification(NOrderEvent event) {

        /*
         * VALIDATION
         */
        if (event == null || event.getOrderId() == null || event.getOutletId() == null) {

            throw new NotificationException("Invalid event data");
        }

        /*
         * SUBJECT
         */
        String subject;

        if ("REJECTED".equalsIgnoreCase(event.getStatus())) {

            subject = NConstants.SUBJECT_REJECTED_ORDER;

        } else {

            subject = NConstants.SUBJECT_NEW_ORDER;
        }

        /*
         * FETCH TEMPLATE
         */
        Notification notification = notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject).orElseThrow(() -> new NotificationException("Notification template not found"));

        /*
         * CHECK DUPLICATE
         */
        boolean exists = statusRepository.existsByOrderIdAndNotificationRecipientId(event.getOrderId(), event.getOutletId());

        if (exists) {

            log.info("Duplicate notification skipped orderId={}", event.getOrderId());

            return statusRepository.findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(event.getOrderId(), event.getOutletId()).orElseThrow(() -> new NotificationException("Notification status not found"));
        }

        /*
         * SAVE STATUS
         */
        OrderNotificationStatus status = OrderNotificationStatus.builder()

                .orderId(event.getOrderId())

                .notificationId(notification.getNotificationId())

                .notificationRecipientId(event.getOutletId())

                .recipientType(NConstants.ROLE_OUTLET)

                .notificationStatus(false)

                .createdAt(LocalDateTime.now())

                .createdBy(1)

                .build();

        statusRepository.save(status);

        log.info("Notification stored orderId={}", event.getOrderId());

        return status;
    }

    @Transactional
    public void markAsSent(String orderId, Integer recipientId) {

        OrderNotificationStatus status = statusRepository.findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(orderId, recipientId).orElseThrow(() -> new NotificationException("Notification status not found"));

        status.setNotificationStatus(true);

        status.setUpdatedAt(LocalDateTime.now());

        status.setUpdatedBy(1);

        statusRepository.save(status);

        log.info("Notification marked SENT orderId={}", orderId);
    }

    public Notification getNotificationTemplate(String subject) {

        return notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject).orElseThrow(() -> new NotificationException("Notification template not found"));
    }
}