package com.jippy.notification.repository;

import com.jippy.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {

    Optional<Notification> findByRoleAndSubject(String role, String subject);
}
