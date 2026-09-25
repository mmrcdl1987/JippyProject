        package com.jippy.notification.repository;

import com.jippy.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Integer>,
        JpaSpecificationExecutor<Notification> {

    Optional<Notification> findByRoleAndSubject(
            String role,
            String subject
    );

    Optional<Notification> findByRoleAndSubjectAndIsActiveTrue(
            String role,
            String subject
    );

    Optional<Notification> findByRoleAndNotificationTypeAndIsActiveTrue(
            String role,
            String notificationType
    );

    boolean existsByNotificationTypeAndRoleAndIsActiveTrue(
            String notificationType,
            String role
    );

    boolean existsByNotificationTypeAndRoleAndIsActiveTrueAndNotificationIdNot(
            String notificationType,
            String role,
            Integer notificationId
    );
}
