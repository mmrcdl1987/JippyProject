package com.jippy.notification.repository;

import com.jippy.notification.entity.OrderNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderNotificationStatusRepository
        extends JpaRepository<OrderNotificationStatus, Integer> {

    Optional<OrderNotificationStatus>
    findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
            String orderId,
            Integer notificationRecipientId
    );

    boolean existsByOrderIdAndNotificationRecipientId(
            String orderId,
            Integer notificationRecipientId
    );

    boolean existsByOrderIdAndNotificationRecipientIdAndNotificationId(
            String orderId,
            Integer notificationRecipientId,
            Integer notificationId
    );

    /**
     * Generic notification duplicate check.
     */
    boolean existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
            String referenceType,
            Integer referenceId,
            Integer notificationRecipientId
    );

    /**
     * Latest notification for a reference.
     */
    Optional<OrderNotificationStatus>
    findTopByReferenceTypeAndReferenceIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
            String referenceType,
            Integer referenceId,
            Integer notificationRecipientId
    );
}