package com.jippy.notification.service;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.NotificationRecipient;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.exception.NotificationException;
import com.jippy.notification.repository.NotificationRecipientRepository;
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
    private final NotificationRecipientRepository recipientRepository;
    private final OrderNotificationStatusRepository statusRepository;

    @Transactional
    public Notification processNotification(NOrderEvent event) {

        // Null validation
        if (event == null || event.getOrderId() == null || event.getOutletId() == null) {
            log.error("Invalid event data");
            throw new NotificationException("Invalid order event data");
        }

        log.info("Processing notification | orderId={}, outletId={}", event.getOrderId(), event.getOutletId());

        try {
            // FETCH TEMPLATE (ADMIN CREATED)
            Notification notification = notificationRepository
                    .findByRoleAndSubject(
                            NConstants.ROLE_OUTLET,
                            NConstants.SUBJECT_NEW_ORDER
                    )
                    .orElseThrow(() -> new NotificationException(
                            "Notification template not found"));

            log.info("Notification template found | notificationId={}", notification.getNotificationId());

            // SAVE RECIPIENT
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setNotificationId(notification.getNotificationId());
            recipient.setRecipientId(event.getOutletId());
            recipient.setCreatedAt(LocalDateTime.now());
            recipient.setCreatedBy(1);

            recipientRepository.save(recipient);

            log.info("Recipient saved | notificationId={}, outletId={}",
                    notification.getNotificationId(), event.getOutletId());

            // SAVE STATUS (FALSE)
            OrderNotificationStatus status = new OrderNotificationStatus();
            status.setOrderId(event.getOrderId());
            status.setNotificationId(notification.getNotificationId());
            status.setNotificationStatus(false);
            status.setChangedAt(LocalDateTime.now());

            statusRepository.save(status);

            log.info("Notification status saved | orderId={}, notificationId={}", 
                    event.getOrderId(), notification.getNotificationId());

            return notification;

        } catch (NotificationException e) {
            log.error("Notification exception | orderId={}, error={}", event.getOrderId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing notification | orderId={}, error={}",
                    event.getOrderId(), e.getMessage(), e);
            throw new NotificationException("Failed to process notification");
        }
    }

    @Transactional
    public void markAsSent(String orderId, Integer notificationId) {

        // Null validation
        if (orderId == null || orderId.isEmpty() || notificationId == null || notificationId <= 0) {
            log.error("Invalid parameters for marking notification as sent | orderId={}, notificationId={}", 
                    orderId, notificationId);
            throw new NotificationException("Invalid parameters for notification status update");
        }

        log.info("Marking notification as sent | orderId={}, notificationId={}", orderId, notificationId);

        try {
            OrderNotificationStatus status = statusRepository
                    .findByOrderIdAndNotificationId(orderId, notificationId)
                    .orElseThrow(() -> new NotificationException("Notification status not found for orderId: " + orderId));

            status.setNotificationStatus(true);
            status.setChangedAt(LocalDateTime.now());

            statusRepository.save(status);

            log.info("Notification marked as SENT successfully | orderId={}, notificationId={}", orderId, notificationId);

        } catch (NotificationException e) {
            log.error("Notification exception | orderId={}, error={}", orderId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error updating status | orderId={}, notificationId={}, error={}", 
                    orderId, notificationId, e.getMessage(), e);
            throw new NotificationException("Failed to update notification status");
        }
    }
}