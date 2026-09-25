package com.jippy.notification.serviceImpl;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NMealReminderDto;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import com.jippy.notification.service.IMealReminderService;
import com.jippy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NMealReminderServiceImpl implements IMealReminderService {

    private final DeviceTokenRepository deviceTokenRepository;

    private final NotificationService notificationService;

    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processMealReminder(NMealReminderDto reminder) {

        log.info("SERVICE_START | PROCESS_MEAL_REMINDER | customerId={}", reminder != null ? reminder.getCustomerId() : null);

        try {

            // VALIDATE REQUEST

            if (reminder == null) {

                log.error("MEAL_REMINDER_FAILED | reminder is null");

                return;
            }

            if (reminder.getCustomerId() == null || reminder.getCustomerId() <= 0) {

                log.error("MEAL_REMINDER_FAILED | Invalid customerId");

                return;
            }

            if (reminder.getReferenceId() == null) {

                log.error("MEAL_REMINDER_FAILED | ReferenceId is null | customerId={}", reminder.getCustomerId());

                return;
            }

            if (reminder.getMealType() == null || reminder.getMealType().isBlank()) {

                log.error("MEAL_REMINDER_FAILED | MealType is empty | customerId={}", reminder.getCustomerId());

                return;
            }

            log.info("Processing Customer : {}", reminder.getCustomerId());

            log.info("Meal Type : {}", reminder.getMealType());

            log.info("Reference Id : {}", reminder.getReferenceId());

            // FETCH ALL CUSTOMER DEVICE TOKENS
            List<NDeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdAndUserType(reminder.getCustomerId(), NConstants.ROLE_CUSTOMER);

            if (deviceTokens == null || deviceTokens.isEmpty()) {

                log.warn("NO_DEVICE_TOKENS_FOUND | customerId={}", reminder.getCustomerId());

                return;
            }

            log.info("DEVICE_TOKENS_FOUND | customerId={} | deviceCount={}", reminder.getCustomerId(), deviceTokens.size());


            // FETCH NOTIFICATION TEMPLATE
            Notification notification = notificationService.getNotificationTemplate(NConstants.ROLE_CUSTOMER, NConstants.MEAL_REMINDER);

            log.info("NOTIFICATION_TEMPLATE_LOADED | " + "notificationId={} | subject={}", notification.getNotificationId(), notification.getSubject());


            // DUPLICATE NOTIFICATION CHECK
            boolean alreadySent = orderNotificationStatusRepository.existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(NConstants.REFERENCE_TYPE_MEAL_REMINDER, reminder.getReferenceId(), reminder.getCustomerId());

            if (alreadySent) {

                log.info("MEAL_REMINDER_ALREADY_SENT | " + "customerId={} | referenceId={}", reminder.getCustomerId(), reminder.getReferenceId());

                return;
            }

            // REPLACE TEMPLATE VARIABLES
            String message = notification.getMessage().replace("{mealType}", reminder.getMealType());

            log.info("MEAL_REMINDER_MESSAGE_CREATED | customerId={}", reminder.getCustomerId());

            // SEND TO ALL CUSTOMER DEVICES
            for (NDeviceToken deviceToken : deviceTokens) {

                if (deviceToken == null) {

                    log.warn("NULL_DEVICE_TOKEN_SKIPPED | customerId={}", reminder.getCustomerId());

                    continue;
                }

                if (deviceToken.getFcmToken() == null || deviceToken.getFcmToken().isBlank()) {

                    log.warn("INVALID_FCM_TOKEN_SKIPPED | " + "customerId={} | deviceTokenId={}", reminder.getCustomerId(), deviceToken.getDeviceTokenId());

                    continue;
                }

                processDeviceNotification(reminder, notification, message, deviceToken);
            }

            log.info("MEAL_REMINDER_PROCESSING_COMPLETED | " + "customerId={} | deviceCount={}", reminder.getCustomerId(), deviceTokens.size());

        } catch (Exception ex) {

            log.error("MEAL_REMINDER_PROCESSING_FAILED | customerId={}", reminder != null ? reminder.getCustomerId() : null, ex);
        }

        log.info("SERVICE_END | PROCESS_MEAL_REMINDER | customerId={}", reminder != null ? reminder.getCustomerId() : null);
    }

    // PROCESS ONE DEVICE
    private void processDeviceNotification(NMealReminderDto reminder, Notification notification, String message, NDeviceToken deviceToken) {

        Integer statusId = null;

        try {

            log.info("MEAL_REMINDER_DEVICE_PROCESSING | " + "customerId={} | deviceTokenId={} | deviceType={}", reminder.getCustomerId(), deviceToken.getDeviceTokenId(), deviceToken.getDeviceType());


            // CREATE NOTIFICATION STATUS FOR THIS DEVICE
            var status = notificationService.saveNotificationStatus(notification.getNotificationId(), reminder.getCustomerId(), NConstants.ROLE_CUSTOMER, reminder.getReferenceId(), NConstants.REFERENCE_TYPE_MEAL_REMINDER, deviceToken.getDeviceTokenId());

            if (status != null) {
                statusId = status.getOrderNotificationStatusId();
            }

            // SEND FIREBASE NOTIFICATION

            String firebaseMessageId = notificationService.sendNotification(deviceToken.getFcmToken(), notification.getSubject(), message);

            log.info("FIREBASE_NOTIFICATION_SENT | " + "customerId={} | deviceTokenId={} | " + "firebaseMessageId={}", reminder.getCustomerId(), deviceToken.getDeviceTokenId(), firebaseMessageId);


            // MARK THIS EXACT DEVICE STATUS AS SENT
            notificationService.markAsSent(statusId, firebaseMessageId);

            log.info("MEAL_REMINDER_DEVICE_SENT | " + "customerId={} | deviceTokenId={} | statusId={}", reminder.getCustomerId(), deviceToken.getDeviceTokenId(), statusId);

        } catch (Exception ex) {

            log.error("MEAL_REMINDER_DEVICE_FAILED | " + "customerId={} | deviceTokenId={} | statusId={}", reminder.getCustomerId(), deviceToken.getDeviceTokenId(), statusId, ex);
        }
    }
}