package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmUserOtpRepository extends JpaRepository<FmUserOtp, Long> {

    Optional<FmUserOtp> findTopByUserIdAndUserTypeAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
            Long userId,
            String userType,
            String otpType
    );

    Optional<FmUserOtp> findTopByUserIdAndUserTypeAndOtpTypeOrderByCreatedAtDesc(
            Long userId,
            String userType,
            String otpType
    );
}