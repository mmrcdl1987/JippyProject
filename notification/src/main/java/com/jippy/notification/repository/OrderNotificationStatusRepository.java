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
     * CHECK DUPLICATE
     */
    boolean existsByOrderIdAndNotificationRecipientId(
            String orderId,
            Integer notificationRecipientId
    );
}