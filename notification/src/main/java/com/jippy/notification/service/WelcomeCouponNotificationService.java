package com.jippy.notification.service;

import com.jippy.notification.dto.WelcomeCouponNotificationEvent;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.exception.NotificationException;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.NotificationRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeCouponNotificationService {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String SUBJECT = "WELCOME_COUPON";
    private static final String REFERENCE_TYPE = "WELCOME_COUPON";

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final OrderNotificationStatusRepository statusRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    @KafkaListener(
            topics = "welcome-coupon",
            groupId = "welcome-coupon-group",
            containerFactory = "welcomeCouponKafkaListenerContainerFactory"
    )
    public void consume(WelcomeCouponNotificationEvent event) {
        log.info("KAFKA_START | WELCOME_COUPON_NOTIFICATION | customerId={}", event != null ? event.getCustomerId() : null);

        try {
            validateEvent(event);

            Notification notification = notificationRepository.findByRoleAndSubject(CUSTOMER, SUBJECT)
                    .or(() -> notificationRepository.findByRoleAndNotificationTypeAndIsActiveTrue(CUSTOMER, SUBJECT))
                    .orElseThrow(() -> new NotificationException("Notification template not found"));

            boolean alreadySent = statusRepository.existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
                    REFERENCE_TYPE, event.getCouponId(), event.getCustomerId());

            if (alreadySent) {
                log.info("WELCOME_COUPON_ALREADY_SENT | customerId={} | couponId={}", event.getCustomerId(), event.getCouponId());
                return;
            }

            List<NDeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdAndUserType(event.getCustomerId(), CUSTOMER);

            if (deviceTokens == null || deviceTokens.isEmpty()) {
                log.warn("WELCOME_COUPON_NO_DEVICE_TOKENS | customerId={} | couponId={}", event.getCustomerId(), event.getCouponId());
                return;
            }

            for (NDeviceToken deviceToken : deviceTokens) {
                if (deviceToken == null || deviceToken.getFcmToken() == null || deviceToken.getFcmToken().isBlank()) {
                    continue;
                }
                processDeviceNotification(event, notification, deviceToken);
            }

            log.info("KAFKA_END | WELCOME_COUPON_NOTIFICATION_COMPLETED | customerId={} | deviceCount={}",
                    event.getCustomerId(), deviceTokens.size());

        } catch (NotificationException ex) {
            log.error("WELCOME_COUPON_NOTIFICATION_FAILED | customerId={} | error={}",
                    event != null ? event.getCustomerId() : null, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("UNEXPECTED_ERROR | WELCOME_COUPON_NOTIFICATION | customerId={} | error={}",
                    event != null ? event.getCustomerId() : null, ex.getMessage(), ex);
            throw new NotificationException("Failed to process welcome coupon notification", ex);
        }
    }

    private void processDeviceNotification(WelcomeCouponNotificationEvent event, Notification notification, NDeviceToken deviceToken) {
        Integer statusId = null;

        try {
            OrderNotificationStatus status = new OrderNotificationStatus();
            status.setReferenceType(REFERENCE_TYPE);
            status.setReferenceId(event.getCouponId());
            status.setNotificationId(notification.getNotificationId());
            status.setNotificationRecipientId(event.getCustomerId());
            status.setRecipientType(CUSTOMER);
            status.setNotificationStatus(false);
            status.setDeviceTokenId(deviceToken.getDeviceTokenId());
            status.setCreatedAt(LocalDateTime.now());
            status.setCreatedBy(1);

            OrderNotificationStatus savedStatus = statusRepository.save(status);
            statusId = savedStatus.getOrderNotificationStatusId();

            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("customerId", String.valueOf(event.getCustomerId()));
            dataPayload.put("couponId", String.valueOf(event.getCouponId()));
            dataPayload.put("couponCode", String.valueOf(event.getCouponCode()));
            dataPayload.put("discountValue", String.valueOf(event.getDiscountValue()));
            dataPayload.put("minimumOrderValue", String.valueOf(event.getMinOrderValue()));

            String firebaseMessageId = firebaseNotificationService.sendNotification(
                    deviceToken.getFcmToken(), notification.getSubject(), notification.getMessage(), dataPayload);

            savedStatus.setNotificationStatus(true);
            savedStatus.setFirebaseMessageId(firebaseMessageId);
            savedStatus.setSentAt(LocalDateTime.now());
            savedStatus.setUpdatedAt(LocalDateTime.now());
            savedStatus.setUpdatedBy(1);

            statusRepository.save(savedStatus);

            log.info("WELCOME_COUPON_DEVICE_SUCCESS | customerId={} | deviceTokenId={} | statusId={}",
                    event.getCustomerId(), deviceToken.getDeviceTokenId(), statusId);

        } catch (Exception ex) {
            log.error("WELCOME_COUPON_DEVICE_FAILED | customerId={} | deviceTokenId={} | statusId={}",
                    event.getCustomerId(), deviceToken.getDeviceTokenId(), statusId, ex);
        }
    }

    private void validateEvent(WelcomeCouponNotificationEvent event) {
        if (event == null) {
            throw new NotificationException("Event cannot be null");
        }
        if (event.getCustomerId() == null || event.getCustomerId() <= 0) {
            throw new NotificationException("Invalid customer id");
        }
        if (event.getCouponId() == null) {
            throw new NotificationException("Coupon id cannot be null");
        }
        if (event.getCouponCode() == null || event.getCouponCode().isBlank()) {
            throw new NotificationException("Coupon code cannot be empty");
        }
    }
}