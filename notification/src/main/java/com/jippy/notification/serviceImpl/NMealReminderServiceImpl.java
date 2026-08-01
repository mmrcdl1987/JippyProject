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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NMealReminderServiceImpl implements IMealReminderService {

    private final DeviceTokenRepository deviceTokenRepository;

    private final NotificationService notificationService;

    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processMealReminder(NMealReminderDto reminder) {

        log.info("SERVICE_START | PROCESS_MEAL_REMINDER");

        try {

            log.info("----------------------------------------------------");
            log.info("Processing Customer : {}", reminder.getCustomerId());
            log.info("Meal Type : {}", reminder.getMealType());
            log.info("Reference Id : {}", reminder.getReferenceId());

            /*
             * STEP-1
             * Fetch Device Token
             */
            Optional<NDeviceToken> deviceTokenOptional =
                    deviceTokenRepository.findByUserIdAndUserType(
                            reminder.getCustomerId(),
                            NConstants.ROLE_CUSTOMER
                    );

            if (deviceTokenOptional.isEmpty()) {

                log.warn("No device token found for customer {}", reminder.getCustomerId());
                return;
            }

            NDeviceToken deviceToken = deviceTokenOptional.get();

            log.info("Device Token Found : {}", deviceToken.getDeviceTokenId());

            /*
             * STEP-2
             * Fetch Notification Template
             */
            Notification notification =
                    notificationService.getNotificationTemplate(
                            NConstants.ROLE_CUSTOMER,
                            NConstants.MEAL_REMINDER
                    );

            log.info("Notification Template Loaded");
            log.info("Subject : {}", notification.getSubject());

            /*
             * STEP-3
             * Duplicate Notification Check
             */
            boolean alreadySent =
                    orderNotificationStatusRepository
                            .existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
                                    NConstants.REFERENCE_TYPE_MEAL_REMINDER,
                                    reminder.getReferenceId(),
                                    reminder.getCustomerId()
                            );

            if (alreadySent) {

                log.info("Meal reminder already sent for customer {}", reminder.getCustomerId());

                return;
            }

            /*
             * STEP-4
             * Replace Template Variables
             */
            String message = notification.getMessage()
                    .replace("{mealType}", reminder.getMealType());

            /*
             * STEP-5
             * Save Notification Status
             */
            notificationService.saveNotificationStatus(
                    notification.getNotificationId(),
                    reminder.getCustomerId(),
                    NConstants.ROLE_CUSTOMER,
                    reminder.getReferenceId(),
                    NConstants.REFERENCE_TYPE_MEAL_REMINDER,
                    deviceToken.getDeviceTokenId()
            );

            /*
             * STEP-6
             * Send Firebase Notification
             */
            String firebaseMessageId =
                    notificationService.sendNotification(
                            deviceToken.getFcmToken(),
                            notification.getSubject(),
                            message
                    );

            log.info("Firebase Message Id : {}", firebaseMessageId);

            /*
             * STEP-7
             * Mark Notification As Sent
             */
            notificationService.markAsSent(
                    NConstants.REFERENCE_TYPE_MEAL_REMINDER,
                    reminder.getReferenceId(),
                    reminder.getCustomerId(),
                    firebaseMessageId
            );

            log.info("Meal Reminder Sent Successfully For Customer : {}", reminder.getCustomerId());

        } catch (Exception ex) {

            log.error("Failed Processing Meal Reminder For Customer : {}", reminder.getCustomerId(), ex);
        }

        log.info("SERVICE_END | PROCESS_MEAL_REMINDER");
    }
}