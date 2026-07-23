package com.jippy.foodandmart.entity;

import com.jippy.foodandmart.enums.FmOtpPurpose;
import com.jippy.foodandmart.enums.FmOtpStatus;
import com.jippy.foodandmart.enums.FmUserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_otp_verifications", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmEmailOtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_otp_verifications_id")
    private Integer emailOtpVerificationId;

    /**
     * MERCHANT / OUTLET
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private FmUserType entityType;

    /**
     * merchant_id or outlet_id
     */
    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    /**
     * BCrypt encrypted OTP
     */
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    /**
     * SIGNUP
     * CREATE_OUTLET
     * FORGOT_PASSWORD
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 255)
    private FmOtpPurpose purpose;

    /**
     * PENDING
     * VERIFIED
     * FAILED
     * EXPIRED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 100)
    private FmOtpStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "no_of_attempts", nullable = false)
    private Integer noOfAttempts;

    @Column(name = "resend_count")
    private Integer resendCount;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = FmOtpStatus.PENDING;
        }

        if (this.isVerified == null) {
            this.isVerified = false;
        }

        if (this.noOfAttempts == null) {
            this.noOfAttempts = 0;
        }

        if (this.resendCount == null) {
            this.resendCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}