//package com.jippy.notification.repository;
//
//import com.jippy.notification.entity.NDeviceToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface NDeviceTokenRepository extends JpaRepository<NDeviceToken, Integer> {
//
//    /**
//     * Find a specific device by user, user type and device type.
//     */
//    Optional<NDeviceToken> findByUserIdAndUserTypeAndDeviceType(
//            Integer userId,
//            String userType,
//            String deviceType
//    );
//
//    /**
//     * Find all devices for a user.
//     */
////    List<NDeviceToken> findByUserIdAndUserType(
////            Integer userId,
////            String userType
////    );
//
//}