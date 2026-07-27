package com.jippy.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @KafkaListener(
            topics = "welcome-coupon",
            groupId = "welcome-coupon-group"
    )
    @Transactional
    public void consume(WelcomeCouponNotificationEvent event) {

        log.info("KAFKA_START | WELCOME_COUPON_NOTIFICATION | customerId={}",
                event != null ? event.getCustomerId() : null);

        try {

            validateEvent(event);

            NDeviceToken deviceToken = deviceTokenRepository
                    .findByUserIdAndUserType(event.getCustomerId(), CUSTOMER)
                    .orElseThrow(() ->
                            new NotificationException("Device token not found"));

            Notification notification = notificationRepository
                    .findByRoleAndSubject(CUSTOMER, SUBJECT)
                    .orElseThrow(() ->
                            new NotificationException("Notification template not found"));

            boolean alreadySent = statusRepository
                    .existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
                            REFERENCE_TYPE,
                            event.getCouponId(),
                            event.getCustomerId());

            if (alreadySent) {

                log.info("WELCOME_COUPON_ALREADY_SENT | customerId={} | couponId={}",
                        event.getCustomerId(),
                        event.getCouponId());

                return;
            }

            sendFirebaseNotification(deviceToken, notification, event);

            OrderNotificationStatus status = new OrderNotificationStatus();

            status.setReferenceType(REFERENCE_TYPE);
            status.setReferenceId(event.getCouponId());
            status.setOrderId(null);

            status.setNotificationId(notification.getNotificationId());

            status.setNotificationRecipientId(event.getCustomerId());
            status.setRecipientType(CUSTOMER);

            status.setNotificationStatus(true);

            status.setDeviceTokenId(deviceToken.getDeviceTokenId());

            status.setCreatedAt(LocalDateTime.now());
            status.setCreatedBy(1);

            statusRepository.save(status);

            log.info("KAFKA_END | WELCOME_COUPON_NOTIFICATION_SUCCESS | customerId={} | couponCode={}",
                    event.getCustomerId(),
                    event.getCouponCode());

        } catch (NotificationException ex) {

            log.error("WELCOME_COUPON_NOTIFICATION_FAILED | customerId={} | error={}",
                    event != null ? event.getCustomerId() : null,
                    ex.getMessage(),
                    ex);

            throw ex;

        } catch (Exception ex) {

            log.error("UNEXPECTED_ERROR | customerId={} | error={}",
                    event != null ? event.getCustomerId() : null,
                    ex.getMessage(),
                    ex);

            throw new NotificationException("Failed to process welcome coupon notification");
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

    private void sendFirebaseNotification(NDeviceToken deviceToken,
                                          Notification notification,
                                          WelcomeCouponNotificationEvent event) {

        try {

            Message message = Message.builder()
                    .setToken(deviceToken.getFcmToken())
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(notification.getSubject())
                                    .setBody(notification.getMessage())
                                    .build()
                    )
                    .putData("customerId", String.valueOf(event.getCustomerId()))
                    .putData("couponId", String.valueOf(event.getCouponId()))
                    .putData("couponCode", event.getCouponCode())
                    .putData("discountValue", String.valueOf(event.getDiscountValue()))
                    .putData("minimumOrderValue", String.valueOf(event.getMinOrderValue()))
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            log.info("FCM_NOTIFICATION_SENT | response={}", response);

        } catch (FirebaseMessagingException ex) {

            log.error("FCM_NOTIFICATION_FAILED", ex);

            throw new NotificationException("Failed to send Firebase notification");
        }
    }
}