package com.jippy.notification.service;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OrderNotificationStatusRepository statusRepository;
    private final WalletNotificationStatusRepository walletStatusRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    // ORDER NOTIFICATION


    @Transactional
    public void processOrderNotification(NOrderEvent event) {
        log.info("SERVICE_START | PROCESS_ORDER_NOTIFICATION | orderId={} | customerId={} | outletId={}",
                event != null ? event.getOrderId() : null,
                event != null ? event.getCustomerId() : null,
                event != null ? event.getOutletId() : null);

        if (event == null || event.getOrderId() == null) {
            log.error("VALIDATION_FAILED | ORDER_EVENT_NULL");
            return;
        }

        String notificationType = resolveOrderNotificationType(event);

        // 1. Customer notification dispatch (if customerId present)
        if (event.getCustomerId() != null && event.getCustomerId() > 0) {
            dispatchOrderNotificationToCustomer(event, notificationType);
        }

        // 2. Outlet notification dispatch (if outletId present)
        if (event.getOutletId() != null && event.getOutletId() > 0) {
            dispatchOrderNotificationToOutlet(event, notificationType);
        }
    }

    private void dispatchOrderNotificationToCustomer(NOrderEvent event, String notificationType) {
        try {
            Notification template = notificationRepository
                    .findByRoleAndNotificationTypeAndIsActiveTrue(NConstants.ROLE_CUSTOMER, notificationType)
                    .orElse(null);

            if (template == null) {
                log.warn("TEMPLATE_NOT_FOUND | role=CUSTOMER | type={}", notificationType);
                return;
            }

            List<NDeviceToken> devices = deviceTokenRepository.findAllByUserIdAndUserType(
                    event.getCustomerId(), NConstants.ROLE_CUSTOMER);

            if (devices.isEmpty()) {
                log.warn("NO_CUSTOMER_DEVICE_TOKENS | customerId={}", event.getCustomerId());
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("orderId", String.valueOf(event.getOrderId()));
            if (event.getStatus() != null)
                params.put("status", event.getStatus());

            String resolvedMessage = interpolateMessage(template.getMessage(), params);

            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("orderId", String.valueOf(event.getOrderId()));
            dataPayload.put("notificationType", notificationType);
            dataPayload.put("role", NConstants.ROLE_CUSTOMER);

            for (NDeviceToken device : devices) {
                if (device.getFcmToken() == null || device.getFcmToken().isBlank())
                    continue;

                OrderNotificationStatus status = OrderNotificationStatus.builder()
                        .orderId(event.getOrderId())
                        .notificationId(template.getNotificationId())
                        .notificationRecipientId(event.getCustomerId())
                        .recipientType(NConstants.ROLE_CUSTOMER)
                        .deviceTokenId(device.getDeviceTokenId())
                        .notificationStatus(false)
                        .createdAt(LocalDateTime.now())
                        .createdBy(1)
                        .build();

                status = statusRepository.save(status);

                try {
                    String messageId = firebaseNotificationService.sendNotification(
                            device.getFcmToken(), template.getSubject(), resolvedMessage, dataPayload);

                    status.setNotificationStatus(true);
                    status.setFirebaseMessageId(messageId);
                    status.setSentAt(LocalDateTime.now());
                    status.setUpdatedAt(LocalDateTime.now());
                    statusRepository.save(status);
                } catch (Exception ex) {
                    log.error("CUSTOMER_ORDER_PUSH_FAILED | deviceTokenId={} | orderId={}",
                            device.getDeviceTokenId(), event.getOrderId(), ex);
                    status.setNotificationStatus(false);
                    status.setFirebaseMessageId("SEND_FAILED");
                    status.setUpdatedAt(LocalDateTime.now());
                    statusRepository.save(status);
                }
            }
        } catch (Exception ex) {
            log.error("CUSTOMER_ORDER_DISPATCH_FAILED | orderId={}", event.getOrderId(), ex);
        }
    }

    private void dispatchOrderNotificationToOutlet(NOrderEvent event, String notificationType) {
        try {
            Notification template = notificationRepository
                    .findByRoleAndSubject(NConstants.ROLE_OUTLET, notificationType)
                    .or(() -> notificationRepository
                            .findByRoleAndNotificationTypeAndIsActiveTrue(NConstants.ROLE_OUTLET, notificationType))
                    .orElse(null);

            if (template == null) {
                log.warn("OUTLET_TEMPLATE_NOT_FOUND | type={}", notificationType);
                return;
            }

            boolean alreadyExists = statusRepository.existsByOrderIdAndNotificationRecipientIdAndNotificationId(
                    event.getOrderId(), event.getOutletId(), template.getNotificationId());

            if (alreadyExists) {
                log.info("DUPLICATE_OUTLET_NOTIFICATION_SKIPPED | orderId={} | outletId={} | notificationId={}",
                        event.getOrderId(), event.getOutletId(), template.getNotificationId());
                return;
            }

            OrderNotificationStatus status = OrderNotificationStatus.builder()
                    .orderId(event.getOrderId())
                    .notificationId(template.getNotificationId())
                    .notificationRecipientId(event.getOutletId())
                    .recipientType(NConstants.ROLE_OUTLET)
                    .deviceTokenId(0) // Default for topic broadcast
                    .notificationStatus(false)
                    .createdAt(LocalDateTime.now())
                    .createdBy(1)
                    .build();

            status = statusRepository.save(status);

            String topic = NConstants.TOPIC_PREFIX + event.getOutletId();
            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("orderId", String.valueOf(event.getOrderId()));
            dataPayload.put("status", String.valueOf(event.getStatus()));
            dataPayload.put("notificationType", notificationType);
            dataPayload.put("orderType", String.valueOf(event.getOrderType()));

            try {
                String messageId = firebaseNotificationService.sendTopicNotification(
                        topic, template.getSubject(), template.getMessage(), dataPayload);

                status.setNotificationStatus(true);
                status.setFirebaseMessageId(messageId);
                status.setSentAt(LocalDateTime.now());
                status.setUpdatedAt(LocalDateTime.now());
                statusRepository.save(status);

                log.info("OUTLET_TOPIC_PUSH_SENT | topic={} | orderId={} | messageId={}", topic, event.getOrderId(), messageId);
            } catch (Exception ex) {
                log.error("OUTLET_ORDER_DISPATCH_FAILED | orderId={}", event.getOrderId(), ex);
                status.setNotificationStatus(false);
                status.setFirebaseMessageId("SEND_FAILED");
                status.setUpdatedAt(LocalDateTime.now());
                statusRepository.save(status);
            }
        } catch (Exception ex) {
            log.error("OUTLET_ORDER_DISPATCH_ERROR | orderId={}", event.getOrderId(), ex);
        }
    }

    private String resolveOrderNotificationType(NOrderEvent event) {
        if (event.getNotificationType() != null && !event.getNotificationType().isBlank()) {
            return event.getNotificationType();
        }
        if ("ACCEPTED".equalsIgnoreCase(event.getStatus()) || "ORDER_ACCEPTED".equalsIgnoreCase(event.getStatus())) {
            return "ORDER_ACCEPTED";
        }
        if ("REJECTED".equalsIgnoreCase(event.getStatus()) || "REJECTED_ORDER".equalsIgnoreCase(event.getStatus())) {
            return "REJECTED_ORDER";
        }
        return "ORDER_CREATED";
    }

    private String interpolateMessage(String message, Map<String, String> values) {
        if (message == null || values == null || values.isEmpty()) {
            return message;
        }
        String resolved = message;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getValue() != null) {
                resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return resolved;
    }

    // LEGACY / OUTLET PROCESS NOTIFICATION (Kept for compatibility)

    @Transactional
    public OrderNotificationStatus processNotification(NOrderEvent event) {
        log.info("SERVICE_START | PROCESS_NOTIFICATION | orderId={} | outletId={}",
                event != null ? event.getOrderId() : null,
                event != null ? event.getOutletId() : null);

        if (event == null) {
            throw new NotificationException("Notification event cannot be null");
        }

        if (event.getOutletId() == null || event.getOutletId() <= 0) {
            throw new NotificationException("Invalid outlet id");
        }

        String subject = getSubject(event);

        Notification notification = notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject)
                .orElseThrow(
                        () -> new NotificationException("Notification template not found for subject: " + subject));

        boolean exists = statusRepository.existsByOrderIdAndNotificationRecipientIdAndNotificationId(
                event.getOrderId(), event.getOutletId(), notification.getNotificationId());

        if (exists) {
            log.info("DUPLICATE_NOTIFICATION_SKIPPED | orderId={} | notificationId={}",
                    event.getOrderId(), notification.getNotificationId());
            return statusRepository.findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
                    event.getOrderId(), event.getOutletId())
                    .orElseThrow(() -> new NotificationException("Notification status not found"));
        }

        OrderNotificationStatus status = OrderNotificationStatus.builder()
                .orderId(event.getOrderId())
                .notificationId(notification.getNotificationId())
                .notificationRecipientId(event.getOutletId())
                .recipientType(NConstants.ROLE_OUTLET)
                .deviceTokenId(0) // Default for topic broadcast
                .notificationStatus(false)
                .createdAt(LocalDateTime.now())
                .createdBy(1)
                .build();

        return statusRepository.save(status);
    }

    // MARK AS SENT OVERLOADS

    @Transactional
    public void markAsSent(Integer statusId, String firebaseMessageId) {
        log.info("SERVICE_START | MARK_NOTIFICATION_SENT_BY_ID | statusId={}", statusId);

        OrderNotificationStatus status = statusRepository.findById(statusId)
                .orElseThrow(() -> new NotificationException("Notification status not found with ID: " + statusId));

        status.setNotificationStatus(true);
        status.setFirebaseMessageId(firebaseMessageId);
        status.setSentAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());
        status.setUpdatedBy(1);

        statusRepository.save(status);
        log.info("SERVICE_END | MARK_NOTIFICATION_SENT_BY_ID_SUCCESS | statusId={}", statusId);
    }

    @Transactional
    public void markAsSent(String orderId, Integer recipientId) {
        log.info("SERVICE_START | MARK_NOTIFICATION_SENT | orderId={} | recipientId={}", orderId, recipientId);

        OrderNotificationStatus status = statusRepository
                .findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(orderId, recipientId)
                .orElseThrow(() -> new NotificationException("Notification status not found"));

        status.setNotificationStatus(true);
        status.setSentAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());
        status.setUpdatedBy(1);

        statusRepository.save(status);
        log.info("SERVICE_END | MARK_NOTIFICATION_SENT_SUCCESS | orderId={}", orderId);
    }

    @Transactional
    public void markAsSent(String referenceType, Integer referenceId, Integer recipientId, String firebaseMessageId) {
        log.info("SERVICE_START | MARK_NOTIFICATION_SENT | referenceType={} | referenceId={} | recipientId={}",
                referenceType, referenceId, recipientId);

        OrderNotificationStatus status = statusRepository
                .findTopByReferenceTypeAndReferenceIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
                        referenceType, referenceId, recipientId)
                .orElseThrow(() -> new NotificationException("Notification status not found"));

        status.setNotificationStatus(true);
        status.setFirebaseMessageId(firebaseMessageId);
        status.setSentAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());
        status.setUpdatedBy(1);

        statusRepository.save(status);
        log.info("SERVICE_END | MARK_NOTIFICATION_SENT_SUCCESS | referenceId={}", referenceId);
    }

    // NOTIFICATION TEMPLATES

    public Notification getNotificationTemplate(String subject) {
        return notificationRepository.findByRoleAndSubject(NConstants.ROLE_OUTLET, subject)
                .orElseThrow(
                        () -> new NotificationException("Notification template not found for subject: " + subject));
    }

    public Notification getNotificationTemplate(String role, String subject) {
        return notificationRepository.findByRoleAndSubject(role, subject)
                .or(() -> notificationRepository.findByRoleAndNotificationTypeAndIsActiveTrue(role, subject))
                .orElseThrow(() -> new NotificationException(
                        "Notification template not found for role=" + role + " subject=" + subject));
    }

    public Notification getNotificationTemplateByType(String role, String notificationType) {
        return notificationRepository.findByRoleAndNotificationTypeAndIsActiveTrue(role, notificationType)
                .orElseThrow(() -> new NotificationException(
                        "Notification template not found for type : " + notificationType));
    }

    private String getSubject(NOrderEvent event) {
        if (event.getNotificationType() != null && !event.getNotificationType().isBlank()) {
            return event.getNotificationType();
        }
        if ("REJECTED".equalsIgnoreCase(event.getStatus())) {
            return "REJECTED_ORDER";
        }
        return "ORDER_CREATED";
    }
    // ORDER NOTIFICATION STATUS SAVE
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
                .deviceTokenId(deviceTokenId != null ? deviceTokenId : 0)
                .notificationStatus(false)
                .createdAt(LocalDateTime.now())
                .createdBy(1)
                .build();

        return statusRepository.save(status);
    }

    // FIREBASE DISPATCH WRAPPER

    public String sendNotification(String token, String title, String body) {
        return firebaseNotificationService.sendNotification(token, title, body);
    }
    // WALLET POINTS NOTIFICATION (Idempotent & Multi-Device)

    @Transactional
    public void processWalletPointsNotification(NWalletPointsEvent event) {
        log.info("PROCESS_WALLET_POINTS_NOTIFICATION_START | orderId={} | customerId={} | pointsType={}",
                event != null ? event.getOrderId() : null,
                event != null ? event.getCustomerId() : null,
                event != null ? event.getPointsType() : null);

        if (event == null) {
            throw new NotificationException("Wallet points notification event cannot be null");
        }

        if (event.getCustomerId() == null || event.getCustomerId() <= 0) {
            throw new NotificationException("Invalid customer id");
        }

        Notification notification = getNotificationTemplateByType(
                NConstants.ROLE_CUSTOMER, event.getNotificationType());

        // Multi-device lookup
        List<NDeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdAndUserType(
                event.getCustomerId(), NConstants.ROLE_CUSTOMER);

        if (deviceTokens.isEmpty()) {
            log.warn("CUSTOMER_DEVICE_TOKENS_NOT_FOUND | customerId={}", event.getCustomerId());
            return;
        }

        String message = NWalletNotificationMapper.buildWalletPointsMessage(notification, event);

        // Correct Idempotency Check: query walletStatusRepository (not
        // statusRepository)
        boolean alreadyExists = walletStatusRepository.existsByOrderIdAndNotificationIdAndNotificationRecipientId(
                event.getOrderId(), notification.getNotificationId(), event.getCustomerId());

        if (alreadyExists) {
            log.info("DUPLICATE_WALLET_NOTIFICATION_SKIPPED | orderId={} | customerId={} | notificationId={}",
                    event.getOrderId(), event.getCustomerId(), notification.getNotificationId());
            return;
        }

        for (NDeviceToken deviceToken : deviceTokens) {
            if (deviceToken.getFcmToken() == null || deviceToken.getFcmToken().isBlank()) {
                continue;
            }

            WalletNotificationStatus status = NWalletNotificationMapper.toNotificationStatus(
                    event, notification, deviceToken);
            status = walletStatusRepository.save(status);

            try {
                String firebaseMessageId = firebaseNotificationService.sendNotification(
                        deviceToken.getFcmToken(), notification.getSubject(), message);

                status.setNotificationStatus(true);
                status.setFirebaseMessageId(firebaseMessageId);
                status.setSentAt(LocalDateTime.now());
                status.setUpdatedAt(LocalDateTime.now());
                walletStatusRepository.save(status);

                log.info(
                        "WALLET_POINTS_NOTIFICATION_SENT | orderId={} | customerId={} | deviceTokenId={} | messageId={}",
                        event.getOrderId(), event.getCustomerId(), deviceToken.getDeviceTokenId(), firebaseMessageId);

            } catch (Exception ex) {
                log.error("WALLET_POINTS_NOTIFICATION_SEND_FAILED | deviceTokenId={} | customerId={}",
                        deviceToken.getDeviceTokenId(), event.getCustomerId(), ex);

                status.setNotificationStatus(false);
                status.setFirebaseMessageId("SEND_FAILED");
                status.setUpdatedAt(LocalDateTime.now());
                walletStatusRepository.save(status);
            }
        }
    }
}
