package com.jippy.notification.repository;

import com.jippy.notification.entity.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient,Integer> {

}
