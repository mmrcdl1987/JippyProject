package com.jippy.notification.serviceImpl;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.dto.NCartReminderDto;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.repository.OrderNotificationStatusRepository;
import com.jippy.notification.service.ICartReminderService;
import com.jippy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NCartReminderServiceIpml implements ICartReminderService {

    private final DeviceTokenRepository deviceTokenRepository;

    private final NotificationService notificationService;

    private final OrderNotificationStatusRepository orderNotificationStatusRepository;

    @Override
    public void processReminder(NCartReminderDto reminder) {

        log.info("SERVICE_START | PROCESS_CART_REMINDER");

        try {

            log.info("----------------------------------------------------");
            log.info("Processing Customer : {}", reminder.getCustomerId());
            log.info("Cart Total : {}", reminder.getCartTotal());
            log.info("Notification Subject : {}", reminder.getNotificationSubject());

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
                            reminder.getNotificationSubject()
                    );

            log.info("Notification Template Loaded");
            log.info("Subject : {}", notification.getSubject());
            log.info("Message : {}", notification.getMessage());

            /*
             * STEP-3
             * Duplicate Notification Check
             */
            boolean alreadySent =
                    orderNotificationStatusRepository
                            .existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
                                    NConstants.REFERENCE_TYPE_CART,
                                    reminder.getCustomerId(),
                                    reminder.getCustomerId()
                            );

            if (alreadySent) {

                log.info("Cart reminder already sent for customer {}",
                        reminder.getCustomerId());

                return;
            }

            /*
             * STEP-4
             * Save Notification Status
             */
            notificationService.saveNotificationStatus(
                    notification.getNotificationId(),
                    reminder.getCustomerId(),
                    NConstants.ROLE_CUSTOMER,
                    reminder.getCustomerId(),
                    NConstants.REFERENCE_TYPE_CART,
                    deviceToken.getDeviceTokenId()
            );

            /*
             * STEP-5
             * Send Firebase Notification
             */
            String firebaseMessageId =
                    notificationService.sendNotification(
                            deviceToken.getFcmToken(),
                            notification.getSubject(),
                            notification.getMessage()
                    );

            log.info("Firebase Message Id : {}", firebaseMessageId);

            /*
             * STEP-6
             * Mark Notification As Sent
             */
            notificationService.markAsSent(
                    NConstants.REFERENCE_TYPE_CART,
                    reminder.getCustomerId(),
                    reminder.getCustomerId(),
                    firebaseMessageId
            );

            log.info("Cart Reminder Sent Successfully For Customer : {}",
                    reminder.getCustomerId());

        } catch (Exception ex) {

            log.error("Failed Processing Customer : {}",
                    reminder.getCustomerId(), ex);
        }

        log.info("SERVICE_END | PROCESS_CART_REMINDER");
    }
}