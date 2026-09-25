package com.jippy.notification.serviceImpl;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NCartReminderDto;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.entity.OrderNotificationStatus;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import com.jippy.notification.service.ICartReminderService;
import com.jippy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NCartReminderServiceImpl implements ICartReminderService {

    private final DeviceTokenRepository deviceTokenRepository;

    private final NotificationService notificationService;

    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processReminder(NCartReminderDto reminder) {

        log.info("SERVICE_START | PROCESS_CART_REMINDER | customerId={}", reminder != null ? reminder.getCustomerId() : null);

        try {

            // VALIDATE REQUEST
            if (reminder == null) {

                log.error("CART_REMINDER_FAILED | REMINDER_NULL");

                return;
            }

            if (reminder.getCustomerId() == null || reminder.getCustomerId() <= 0) {

                log.error("CART_REMINDER_FAILED | INVALID_CUSTOMER_ID");

                return;
            }

            Integer customerId = reminder.getCustomerId();

            log.info("Processing Customer : {}", customerId);

            log.info("Cart Total : {}", reminder.getCartTotal());

            log.info("Notification Subject : {}", reminder.getNotificationSubject());

            // FETCH ALL CUSTOMER DEVICE TOKENS

            List<NDeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdAndUserType(customerId, NConstants.ROLE_CUSTOMER);

            if (deviceTokens == null || deviceTokens.isEmpty()) {

                log.warn("NO_DEVICE_TOKENS_FOUND | customerId={}", customerId);

                return;
            }

            log.info("DEVICE_TOKENS_FOUND | customerId={} | deviceCount={}", customerId, deviceTokens.size());


            // FETCH NOTIFICATION TEMPLATE
            Notification notification = notificationService.getNotificationTemplate(NConstants.ROLE_CUSTOMER, reminder.getNotificationSubject());

            log.info("NOTIFICATION_TEMPLATE_LOADED | " + "notificationId={} | subject={}", notification.getNotificationId(), notification.getSubject());

            // DUPLICATE NOTIFICATION CHECK

            boolean alreadySent = orderNotificationStatusRepository.existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(NConstants.REFERENCE_TYPE_CART, customerId, customerId);

            if (alreadySent) {

                log.info("CART_REMINDER_ALREADY_SENT | customerId={}", customerId);

                return;
            }

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

            log.info("CART_REMINDER_PROCESSING_COMPLETED | " + "customerId={} | deviceCount={}", customerId, deviceTokens.size());

        } catch (Exception ex) {

            log.error("CART_REMINDER_PROCESSING_FAILED | customerId={}", reminder != null ? reminder.getCustomerId() : null, ex);
        }

        log.info("SERVICE_END | PROCESS_CART_REMINDER | customerId={}", reminder != null ? reminder.getCustomerId() : null);
    }

    // PROCESS ONE DEVICE
    private void processDeviceNotification(Integer customerId, Notification notification, NDeviceToken deviceToken) {

        Integer statusId = null;

        try {

            log.info("CART_REMINDER_DEVICE_PROCESSING | " + "customerId={} | deviceTokenId={} | deviceType={}", customerId, deviceToken.getDeviceTokenId(), deviceToken.getDeviceType());

            // SAVE NOTIFICATION STATUS
            OrderNotificationStatus status = notificationService.saveNotificationStatus(notification.getNotificationId(), customerId, NConstants.ROLE_CUSTOMER, customerId, NConstants.REFERENCE_TYPE_CART, deviceToken.getDeviceTokenId());

            statusId = status.getOrderNotificationStatusId();

            log.info("CART_REMINDER_STATUS_CREATED | " + "statusId={} | customerId={} | deviceTokenId={}", statusId, customerId, deviceToken.getDeviceTokenId());

            // SEND FIREBASE NOTIFICATION
            String firebaseMessageId = notificationService.sendNotification(deviceToken.getFcmToken(), notification.getSubject(), notification.getMessage());

            log.info("CART_REMINDER_FCM_SENT | " + "customerId={} | deviceTokenId={} | " + "firebaseMessageId={}", customerId, deviceToken.getDeviceTokenId(), firebaseMessageId);

            // MARK EXACT DEVICE STATUS AS SENT

            notificationService.markAsSent(statusId, firebaseMessageId);

            log.info("CART_REMINDER_DEVICE_SENT | " + "customerId={} | deviceTokenId={} | statusId={}", customerId, deviceToken.getDeviceTokenId(), statusId);

        } catch (Exception ex) {

            /*
             * IMPORTANT:
             *
             * Failure on one device must not stop notification
             * delivery to the customer's other devices.
             */

            log.error("CART_REMINDER_DEVICE_FAILED | " + "customerId={} | deviceTokenId={} | statusId={}", customerId, deviceToken.getDeviceTokenId(), statusId, ex);
        }
    }
}