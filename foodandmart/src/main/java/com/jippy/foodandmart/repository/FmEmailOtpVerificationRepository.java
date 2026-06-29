package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.Enum.FmOtpPurpose;
import com.jippy.foodandmart.Enum.FmOtpStatus;
import com.jippy.foodandmart.Enum.FmUserType;
import com.jippy.foodandmart.entity.FmEmailOtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FmEmailOtpVerificationRepository extends JpaRepository<FmEmailOtpVerification, Integer> {

    /**
     * Latest OTP for email & purpose
     */
    Optional<FmEmailOtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            FmOtpPurpose purpose
    );

    /**
     * Latest OTP for entity
     */
    Optional<FmEmailOtpVerification> findTopByEntityTypeAndEntityIdAndPurposeOrderByCreatedAtDesc(
            FmUserType entityType,
            Integer entityId,
            FmOtpPurpose purpose
    );

    /**
     * Check if latest OTP is already verified
     */
    boolean existsByEmailAndPurposeAndIsVerified(
            String email,
            FmOtpPurpose purpose,
            Boolean isVerified
    );

    /**
     * Fetch verified OTP
     */
    Optional<FmEmailOtpVerification> findTopByEmailAndPurposeAndIsVerifiedOrderByVerifiedAtDesc(
            String email,
            FmOtpPurpose purpose,
            Boolean isVerified
    );

    /**
     * Expired OTPs (Scheduler)
     */
    List<FmEmailOtpVerification> findByStatusAndExpiresAtBefore(
            FmOtpStatus status,
            LocalDateTime dateTime
    );

    /**
     * All OTPs of an email
     */
    List<FmEmailOtpVerification> findByEmailOrderByCreatedAtDesc(
            String email
    );

    /**
     * All OTPs of a merchant/outlet
     */
    List<FmEmailOtpVerification> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            FmUserType entityType,
            Integer entityId
    );

}