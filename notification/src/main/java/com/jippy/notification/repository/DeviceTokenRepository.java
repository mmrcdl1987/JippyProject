package com.jippy.notification.repository;

import com.jippy.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository
        extends JpaRepository<DeviceToken, Integer> {

    Optional<DeviceToken> findByUserIdAndUserType(
            Integer userId,
            String userType
    );

    List<DeviceToken> findAllByUserIdAndUserType(
            Integer userId,
            String userType
    );

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    void deleteByUserIdAndUserType(
            Integer userId,
            String userType
    );
}