package com.jippy.notification.repository;

import com.jippy.notification.entity.OrderNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderNotificationStatusRepository
        extends JpaRepository<OrderNotificationStatus, Integer> {

    /*
     * FETCH LATEST STATUS
     */
    Optional<OrderNotificationStatus>
    findTopByOrderIdAndNotificationRecipientIdOrderByOrderNotificationStatusIdDesc(
            String orderId,
            Integer notificationRecipientId
    );

    /*
     * OLD DUPLICATE CHECK
     * KEEP IF USED ELSEWHERE
     */
    boolean existsByOrderIdAndNotificationRecipientId(
            String orderId,
            Integer notificationRecipientId
    );

    /*
     * NEW DUPLICATE CHECK
     * SAME ORDER CAN HAVE:
     * CREATED
     * TODAY REMINDER
     * ONE HOUR REMINDER
     */
    boolean existsByOrderIdAndNotificationRecipientIdAndNotificationId(
            String orderId,
            Integer notificationRecipientId,
            Integer notificationId
    );
}