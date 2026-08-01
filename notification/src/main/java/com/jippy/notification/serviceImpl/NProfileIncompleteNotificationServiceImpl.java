package com.jippy.notification.serviceImpl;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.CoProfileIncompleteCustomer;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import com.jippy.notification.service.NProfileIncompleteNotificationService;
import com.jippy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NProfileIncompleteNotificationServiceImpl
        implements NProfileIncompleteNotificationService {

    private final NotificationService notificationService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processNotification(CoProfileIncompleteCustomer event) {

        log.info("Processing Profile Incomplete Notification for Customer : {}",
                event.getCustomerId());

        // Check whether notification was already sent

        boolean alreadySent =
                orderNotificationStatusRepository
                        .existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
                                NConstants.REFERENCE_TYPE_CUSTOMER,
                                event.getCustomerId(),
                                event.getCustomerId());

        if (alreadySent) {

            log.info("Notification already sent for customer : {}",
                    event.getCustomerId());

            return;
        }

        // Load notification template
        Notification notification = notificationService.getNotificationTemplate(
                NConstants.ROLE_CUSTOMER,
                NConstants.SUBJECT_PROFILE_INCOMPLETE
        );
        log.info("Notification Template Loaded : {}",
                notification.getNotificationId());

        // Find customer's device token
        NDeviceToken deviceToken = deviceTokenRepository
                .findByUserIdAndUserType(
                        event.getCustomerId(),
                        NConstants.ROLE_CUSTOMER
                )
                .orElse(null);

        if (deviceToken == null) {
            log.info("No device token found for customer : {}", event.getCustomerId());
            return;
        }

        // Save notification status
        OrderNotificationStatus status = notificationService.saveNotificationStatus(
                notification.getNotificationId(),
                event.getCustomerId(),
                NConstants.ROLE_CUSTOMER,
                event.getCustomerId(),
                NConstants.REFERENCE_TYPE_CUSTOMER,
                deviceToken.getDeviceTokenId()
        );

        log.info("Notification Status Saved : {}",
                status.getOrderNotificationStatusId());

        try {

            String firebaseMessageId = notificationService.sendNotification(
                    deviceToken.getFcmToken(),
                    notification.getSubject(),
                    notification.getMessage()
            );
            notificationService.markAsSent(
                    NConstants.REFERENCE_TYPE_CUSTOMER,
                    event.getCustomerId(),
                    event.getCustomerId(),
                    firebaseMessageId
            );

            log.info("Profile Incomplete Notification Sent Successfully for Customer : {}",
                    event.getCustomerId());

        } catch (Exception ex) {

            log.error("Failed to send Profile Incomplete Notification for Customer : {}",
                    event.getCustomerId(), ex);
        }
    }
}