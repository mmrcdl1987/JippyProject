package com.jippy.notification.repository;

import com.jippy.notification.entity.WalletNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletNotificationStatusRepository
        extends JpaRepository<WalletNotificationStatus, Integer> {

    boolean existsByOrderIdAndNotificationIdAndNotificationRecipientId(
            String orderId,
            Integer notificationId,
            Integer notificationRecipientId
    );

    boolean existsByReferenceTypeAndReferenceIdAndNotificationRecipientId(
            String referenceType,
            Integer referenceId,
            Integer notificationRecipientId
    );

    Optional<WalletNotificationStatus>
    findTopByReferenceTypeAndReferenceIdAndNotificationRecipientIdOrderByWalletNotificationStatusIdDesc(
            String referenceType,
            Integer referenceId,
            Integer notificationRecipientId
    );
}
