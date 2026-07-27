package com.jippy.notification.repository;

import com.jippy.notification.entity.NDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository
        extends JpaRepository<NDeviceToken, Integer> {

    Optional<NDeviceToken> findByUserIdAndUserType(
            Integer userId,
            String userType
    );

    List<NDeviceToken> findAllByUserIdAndUserType(
            Integer userId,
            String userType
    );

    Optional<NDeviceToken> findByFcmToken(String fcmToken);

    void deleteByUserIdAndUserType(
            Integer userId,
            String userType
    );
    Optional<NDeviceToken> findByUserIdAndUserTypeAndDeviceType(
            Integer userId,
            String userType,
            String deviceType
    );

}