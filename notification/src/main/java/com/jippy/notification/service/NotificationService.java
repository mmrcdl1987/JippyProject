package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NOrderEvent;
import com.jippy.notification.dto.NWalletPointsEvent;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.entity.WalletNotificationStatus;
import com.jippy.notification.exception.NotificationException;
import com.jippy.notification.mapper.NWalletNotificationMapper;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.NotificationRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import com.jippy.notification.repository.WalletNotificationStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String DEVICE_USER_TYPE_CUSTOMER = "CUSTOMER";

    private final NotificationRepository notificationRepository;

    private final OrderNotificationStatusRepository statusRepository;

    private final WalletNotificationStatusRepository walletStatusRepository;

    private final DeviceTokenRepository deviceTokenRepository;
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
        Notification notification =
                notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject)
                        .orElseThrow(() -> {

            log.error("VALIDATION_FAILED | TEMPLATE_NOT_FOUND | subject={}", subject);

            return new NotificationException("Notification template not found");
        });

        /*
         * DUPLICATE CHECK
         */
        boolean exists = statusRepository
                .existsByOrderIdAndNotificationRecipientIdAndNotificationId(event.getOrderId(), event.getOutletId(), notification.getNotificationId());

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
    public Notification getNotificationTemplate(String role, String subject) {

        return notificationRepository
                .findByRoleAndSubject(role, subject)
                .orElseThrow(() ->
                        new NotificationException("Notification template not found"));
    }
    @Transactional
    public OrderNotificationStatus saveNotificationStatus(
            Integer notificationId,
            Integer recipientId,
            String recipientType,
            Integer referenceId,
            String referenceType,
            Integer deviceTokenId) {

        OrderNotificationStatus status = OrderNotificationStatus.builder()
                .notificationId(notificationId)
                .notificationRecipientId(recipientId)
                .recipientType(recipientType)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .deviceTokenId(deviceTokenId)
                .notificationStatus(false)
                .createdAt(LocalDateTime.now())
                .createdBy(1)
                .build();

        return statusRepository.save(status);
    }
    @Transactional
    public void markAsSent(
            String referenceType,
            Integer referenceId,
            Integer recipientId,
            String firebaseMessageId) {

        log.info("SERVICE_START | MARK_NOTIFICATION_SENT | referenceType={} | referenceId={} | recipientId={}",
                referenceType,
                referenceId,
                recipientId);

        OrderNotificationStatus status = statusRepository
                .findTopByReferenceTypeAndReferenceIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
                        referenceType,
                        referenceId,
                        recipientId)
                .orElseThrow(() -> {

                    log.error("NOTIFICATION_STATUS_NOT_FOUND | referenceType={} | referenceId={}",
                            referenceType,
                            referenceId);

                    return new NotificationException("Notification status not found");
                });

        status.setNotificationStatus(true);

        status.setFirebaseMessageId(firebaseMessageId);

        status.setSentAt(LocalDateTime.now());

        status.setUpdatedAt(LocalDateTime.now());

//        status.setUpdatedBy(1);

        statusRepository.save(status);

        log.info("SERVICE_END | MARK_NOTIFICATION_SENT_SUCCESS | referenceId={}", referenceId);
    }
    @Transactional
    public String sendNotification(
            String token,
            String title,
            String body) {

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                    .build();

            String firebaseMessageId = FirebaseMessaging
                    .getInstance()
                    .send(message);

            log.info("NOTIFICATION_SENT | firebaseMessageId={}", firebaseMessageId);

            return firebaseMessageId;

        } catch (Exception ex) {

            log.error("NOTIFICATION_SEND_FAILED", ex);
            throw new NotificationException("Unable to send notification");
        }
    }
    public Notification getNotificationTemplateByType(
            String role,
            String notificationType) {

        log.info("Fetching notification template | role={} | notificationType={}",
                role, notificationType);

        return notificationRepository
                .findByRoleAndNotificationTypeAndIsActiveTrue(role, notificationType)
                .orElseThrow(() ->
                        new NotificationException(
                                "Notification template not found for type : "
                                        + notificationType));
    }
//    ================================================================================================
//    =========================== process Wallet Points Notification =================================
//    ================================================================================================
@Transactional
public void processWalletPointsNotification(NWalletPointsEvent event) {

    log.info(
            "PROCESS_WALLET_POINTS_NOTIFICATION_START | " +
                    "orderId={} | customerId={} | pointsType={}",
            event.getOrderId(),
            event.getCustomerId(),
            event.getPointsType()
    );

    // 1. Get notification template checks for the notification type and role
    Notification notification =
            getNotificationTemplateByType(
                    NConstants.ROLE_CUSTOMER,
                    event.getNotificationType()
            );

    // 2. Save or update the device token when the create-customer flow passes one.
    NDeviceToken deviceToken = upsertCustomerDeviceToken(event);

    if (deviceToken == null) {

        log.warn(
                "CUSTOMER_DEVICE_TOKEN_NOT_FOUND | customerId={}",
                event.getCustomerId()
        );

        return;
    }

    // 3. Prevent duplicate notification
    // checks if a notification for the same order, notification type,
    // and customer has already been sent
    boolean alreadyExists =
            statusRepository
                    .existsByOrderIdAndNotificationIdAndNotificationRecipientId(
                            event.getOrderId(),
                            notification.getNotificationId(),
                            event.getCustomerId()
                    );

    if (alreadyExists) {

        log.info(
                "DUPLICATE_WALLET_NOTIFICATION_SKIPPED | " +
                        "orderId={} | customerId={} | pointsType={}",
                event.getOrderId(),
                event.getCustomerId(),
                event.getPointsType()
        );

        return;
    }

    // 4. Build personalized message for the notification using the
    // event data and notification template
    String message =
            NWalletNotificationMapper
                    .buildWalletPointsMessage(
                            notification,
                            event
                    );
    log.info(
            "WALLET_POINTS_NOTIFICATION_MESSAGE | customerId={} | message={}",
            event.getCustomerId(),
            message
    );

    // 5. Create notification history record in to the database
    WalletNotificationStatus status =
            NWalletNotificationMapper.toNotificationStatus(
                    event,
                    notification,
                    deviceToken
            );

    walletStatusRepository.save(status);

    try {

        // 6. Send Firebase notification and get the message ID
        String firebaseMessageId =
                sendNotification(
                        deviceToken.getFcmToken(),
                        notification.getSubject(),
                        message
                );

        // 7. Mark notification as sent when Firebase notification is successfully sent
        status.setNotificationStatus(true);
        status.setFirebaseMessageId(firebaseMessageId);
        status.setDeliveredAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());
        walletStatusRepository.save(status);

        log.info(
                "WALLET_POINTS_NOTIFICATION_SENT | " +
                        "orderId={} | customerId={} | firebaseMessageId={}",
                event.getOrderId(),
                event.getCustomerId(),
                firebaseMessageId
        );

    } catch (Exception ex) {

        log.error(
                "WALLET_POINTS_NOTIFICATION_SEND_FAILED | orderId={} | customerId={} | error={}",
                event.getOrderId(),
                event.getCustomerId(),
                ex.getMessage(),
                ex
        );

        status.setNotificationStatus(false);
        status.setFirebaseMessageId("FIREBASE_SEND_FAILED");
        status.setUpdatedAt(LocalDateTime.now());
        walletStatusRepository.save(status);
    }
}

    private NDeviceToken upsertCustomerDeviceToken(NWalletPointsEvent event) {

        if (event.getFcmToken() == null || event.getFcmToken().isBlank()) {

            return deviceTokenRepository
                    .findByUserIdAndUserType(
                            event.getCustomerId(),
                            DEVICE_USER_TYPE_CUSTOMER
                    )
                    .orElse(null);
        }

        NDeviceToken deviceToken = deviceTokenRepository
                .findByFcmToken(event.getFcmToken())
                .or(() -> deviceTokenRepository.findByUserIdAndUserType(
                        event.getCustomerId(),
                        DEVICE_USER_TYPE_CUSTOMER
                ))
                .orElseGet(NDeviceToken::new);

        deviceToken.setUserId(event.getCustomerId());
        deviceToken.setUserType(DEVICE_USER_TYPE_CUSTOMER);
        deviceToken.setDeviceType("ANDROID");
        deviceToken.setFcmToken(event.getFcmToken());

        if (deviceToken.getCreatedAt() == null) {
            deviceToken.setCreatedAt(LocalDateTime.now());
        }

        return deviceTokenRepository.save(deviceToken);
    }
}
