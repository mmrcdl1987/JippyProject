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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NProfileIncompleteNotificationServiceImpl implements NProfileIncompleteNotificationService {

    private final NotificationService notificationService;

    private final DeviceTokenRepository deviceTokenRepository;

    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processNotification(CoProfileIncompleteCustomer event) {

        log.info("SERVICE_START | PROFILE_INCOMPLETE_NOTIFICATION | customerId={}", event != null ? event.getCustomerId() : null);

        try {

            // VALIDATE EVENT
            if (event == null) {

                log.error("PROFILE_INCOMPLETE_NOTIFICATION_FAILED | EVENT_NULL");

                return;
            }

            if (event.getCustomerId() == null || event.getCustomerId() <= 0) {

                log.error("PROFILE_INCOMPLETE_NOTIFICATION_FAILED | " + "INVALID_CUSTOMER_ID");

                return;
            }

            Integer customerId = event.getCustomerId();

            log.info("Processing Profile Incomplete Notification | customerId={}", customerId);


            // CHECK DUPLICATE
            boolean alreadySent = orderNotificationStatusRepository.existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(NConstants.REFERENCE_TYPE_CUSTOMER, customerId, customerId);

            if (alreadySent) {

                log.info("PROFILE_INCOMPLETE_NOTIFICATION_ALREADY_SENT | " + "customerId={}", customerId);

                return;
            }

            // LOAD NOTIFICATION TEMPLATE
            Notification notification = notificationService.getNotificationTemplate(NConstants.ROLE_CUSTOMER, NConstants.SUBJECT_PROFILE_INCOMPLETE);

            log.info("NOTIFICATION_TEMPLATE_LOADED | " + "notificationId={} | customerId={}", notification.getNotificationId(), customerId);

            // FETCH ALL CUSTOMER DEVICE TOKENS
            List<NDeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdAndUserType(customerId, NConstants.ROLE_CUSTOMER);

            if (deviceTokens == null || deviceTokens.isEmpty()) {

                log.warn("NO_DEVICE_TOKENS_FOUND | customerId={}", customerId);

                return;
            }

            log.info("DEVICE_TOKENS_FOUND | customerId={} | deviceCount={}", customerId, deviceTokens.size());

            // SEND TO ALL CUSTOMER DEVICES

            for (NDeviceToken deviceToken : deviceTokens) {

                if (deviceToken == null) {

                    log.warn("NULL_DEVICE_TOKEN_SKIPPED | customerId={}", customerId);

                    continue;
                }

                if (deviceToken.getFcmToken() == null || deviceToken.getFcmToken().isBlank()) {

                    log.warn("INVALID_FCM_TOKEN_SKIPPED | " + "customerId={} | deviceTokenId={}", customerId, deviceToken.getDeviceTokenId());

                    continue;
                }

                processDeviceNotification(customerId, notification, deviceToken);
            }

            log.info("SERVICE_END | PROFILE_INCOMPLETE_NOTIFICATION | " + "customerId={} | deviceCount={}", customerId, deviceTokens.size());

        } catch (Exception ex) {

            log.error("PROFILE_INCOMPLETE_NOTIFICATION_FAILED | customerId={}", event != null ? event.getCustomerId() : null, ex);
        }
    }
    // PROCESS ONE DEVICE

    private void processDeviceNotification(Integer customerId, Notification notification, NDeviceToken deviceToken) {

        Integer statusId = null;

        try {

            log.info("PROFILE_INCOMPLETE_DEVICE_PROCESSING | " + "customerId={} | deviceTokenId={} | deviceType={}", customerId, deviceToken.getDeviceTokenId(), deviceToken.getDeviceType());

            // SAVE STATUS FOR THIS DEVICE
            OrderNotificationStatus status = notificationService.saveNotificationStatus(notification.getNotificationId(), customerId, NConstants.ROLE_CUSTOMER, customerId, NConstants.REFERENCE_TYPE_CUSTOMER, deviceToken.getDeviceTokenId());

            statusId = status.getOrderNotificationStatusId();

            log.info("NOTIFICATION_STATUS_SAVED | " + "statusId={} | customerId={} | deviceTokenId={}", statusId, customerId, deviceToken.getDeviceTokenId());


            // SEND FIREBASE NOTIFICATION
            String firebaseMessageId = notificationService.sendNotification(deviceToken.getFcmToken(), notification.getSubject(), notification.getMessage());

            log.info("FCM_NOTIFICATION_SENT | " + "customerId={} | deviceTokenId={} | " + "firebaseMessageId={}", customerId, deviceToken.getDeviceTokenId(), firebaseMessageId);

            // MARK EXACT STATUS AS SENT
            notificationService.markAsSent(statusId, firebaseMessageId);

            log.info("PROFILE_INCOMPLETE_NOTIFICATION_SENT | " + "customerId={} | deviceTokenId={} | statusId={}", customerId, deviceToken.getDeviceTokenId(), statusId);

        } catch (Exception ex) {

            /*
             * One device failure must not stop notification delivery
             * to the customer's other devices.
             */

            log.error("PROFILE_INCOMPLETE_DEVICE_FAILED | " + "customerId={} | deviceTokenId={} | statusId={}", customerId, deviceToken.getDeviceTokenId(), statusId, ex);
        }
    }
}