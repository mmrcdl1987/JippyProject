package com.jippy.notification.repository;

import com.jippy.notification.entity.NDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository
        extends JpaRepository<NDeviceToken, Integer> {

    /**
     * Find a specific FCM token.
     */
    Optional<NDeviceToken> findByFcmToken(String fcmToken);

    /**
     * Find all FCM tokens belonging to a user.
     * One user can have multiple devices/tokens.
     */
    List<NDeviceToken> findAllByUserIdAndUserType(
            Integer userId,
            String userType
    );

    /**
     * Find a specific token belonging to a specific user.
     */
    Optional<NDeviceToken> findByUserIdAndUserTypeAndFcmToken(
            Integer userId,
            String userType,
            String fcmToken
    );

    /**
     * Delete one device/token during logout.
     */
    void deleteByFcmToken(String fcmToken);
}