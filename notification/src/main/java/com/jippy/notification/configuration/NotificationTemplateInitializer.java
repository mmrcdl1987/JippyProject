package com.jippy.notification.configuration;

import com.jippy.notification.constants.NConstants;
import com.jippy.notification.entity.Notification;
import com.jippy.notification.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateInitializer {

    private final NotificationRepository notificationRepository;

    @PostConstruct
    public void seedTemplates() {
        seedCustomerTemplate(
                NConstants.SUBJECT_PROFILE_INCOMPLETE,
                NConstants.SUBJECT_PROFILE_INCOMPLETE,
                "Complete your profile to keep receiving offers",
                "PROFILE_INCOMPLETE"
        );

        seedCustomerTemplate(
                "WELCOME_POINTS_EARNED",
                "WELCOME_POINTS_EARNED",
                "You have earned {points} welcome points",
                "WELCOME_POINTS_EARNED"
        );

        seedCustomerTemplate(
                "REFERRAL_POINTS_EARNED",
                "REFERRAL_POINTS_EARNED",
                "You have earned {points} referral reward points",
                "REFERRAL_POINTS_EARNED"
        );

        seedCustomerTemplate(
                NConstants.NOTIFICATION_TYPE_CREATED,
                NConstants.SUBJECT_NEW_ORDER,
                "Your order has been created",
                NConstants.NOTIFICATION_TYPE_CREATED
        );

        seedCustomerTemplate(
                "POINTS_CONVERTED_TO_MONEY",
                "POINTS_CONVERTED",
                "You have converted {points} points into ₹{amount}",
                "POINTS_CONVERTED_TO_MONEY"
        );
    }

    private void seedCustomerTemplate(
            String notificationType,
            String subject,
            String message,
            String key) {

        notificationRepository.findByRoleAndNotificationTypeAndIsActiveTrue(
                        NConstants.ROLE_CUSTOMER,
                        notificationType)
                .orElseGet(() -> {
                    Notification notification = new Notification();
                    notification.setRole(NConstants.ROLE_CUSTOMER);
                    notification.setSubject(subject);
                    notification.setMessage(message);
                    notification.setNotificationType(notificationType);
                    notification.setPriority("HIGH");
                    notification.setIsActive(true);
                    notification.setCreatedAt(LocalDateTime.now());
                    notification.setCreatedBy(1);
                    log.info("NOTIFICATION_TEMPLATE_SEEDED | type={}", key);
                    return notificationRepository.save(notification);
                });
    }
}
