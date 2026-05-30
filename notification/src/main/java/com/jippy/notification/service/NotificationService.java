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

        log.info("SERVICE_START | PROCESS_NOTIFICATION | orderId={} | outletId={}", event != null ? event.getOrderId() : null, event != null ? event.getOutletId() : null);

        /*
         * VALIDATION
         */
        if (event == null) {

            log.error("VALIDATION_FAILED | EVENT_NULL");

            throw new NotificationException("Notification event cannot be null");
        }

        if (event.getOutletId() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_OUTLET_ID | outletId={}", event.getOutletId());

            throw new NotificationException("Invalid outlet id");
        }

        /*
         * SUBJECT
         */
        String subject = getSubject(event);

        /*
         * FETCH TEMPLATE
         */
        Notification notification = notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject).orElseThrow(() -> {

            log.error("VALIDATION_FAILED | TEMPLATE_NOT_FOUND | subject={}", subject);

            return new NotificationException("Notification template not found");
        });

        /*
         * DUPLICATE CHECK
         */
        boolean exists = statusRepository.existsByOrderIdAndNotificationRecipientIdAndNotificationId(event.getOrderId(), event.getOutletId(), notification.getNotificationId());

        if (exists) {

            log.info("DUPLICATE_NOTIFICATION_SKIPPED | orderId={} | notificationId={}", event.getOrderId(), notification.getNotificationId());

            return statusRepository.findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(event.getOrderId(), event.getOutletId()).orElseThrow(() -> new NotificationException("Notification status not found"));
        }

        /*
         * SAVE STATUS
         */
        OrderNotificationStatus status = OrderNotificationStatus.builder().orderId(event.getOrderId()).notificationId(notification.getNotificationId()).notificationRecipientId(event.getOutletId()).recipientType(NConstants.ROLE_OUTLET).notificationStatus(false).createdAt(LocalDateTime.now()).createdBy(1).build();

        OrderNotificationStatus savedStatus = statusRepository.save(status);

        log.info("SERVICE_END | PROCESS_NOTIFICATION_SUCCESS | orderId={} | notificationStatusId={}", event.getOrderId(), savedStatus.getOrderNotificationStatusId());

        return savedStatus;
    }

    @Transactional
    public void markAsSent(String orderId, Integer recipientId) {

        log.info("SERVICE_START | MARK_NOTIFICATION_SENT | orderId={} | recipientId={}", orderId, recipientId);

        OrderNotificationStatus status = statusRepository.findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(orderId, recipientId).orElseThrow(() -> {

            log.error("VALIDATION_FAILED | NOTIFICATION_STATUS_NOT_FOUND | orderId={}", orderId);

            return new NotificationException("Notification status not found");
        });

        status.setNotificationStatus(true);

        status.setUpdatedAt(LocalDateTime.now());

        status.setUpdatedBy(1);

        statusRepository.save(status);

        log.info("SERVICE_END | MARK_NOTIFICATION_SENT_SUCCESS | orderId={}", orderId);
    }

    public Notification getNotificationTemplate(String subject) {

        log.info("SERVICE_START | GET_NOTIFICATION_TEMPLATE | subject={}", subject);

        Notification notification = notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject).orElseThrow(() -> {

            log.error("VALIDATION_FAILED | TEMPLATE_NOT_FOUND | subject={}", subject);

            return new NotificationException("Notification template not found");
        });

        log.info("SERVICE_END | GET_NOTIFICATION_TEMPLATE_SUCCESS | notificationId={}", notification.getNotificationId());

        return notification;
    }

    /*
     * SUBJECT RESOLVER
     */
    private String getSubject(NOrderEvent event) {

        if (event.getNotificationType() != null && !event.getNotificationType().isBlank()) {

            return event.getNotificationType();
        }

        if ("REJECTED".equalsIgnoreCase(event.getStatus())) {

            return "REJECTED_ORDER";
        }

        return "ORDER_CREATED";
    }
}