package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_otp", schema = "jippy_customer_and_order", indexes = {@Index(name = "idx_customer_otp_customer", columnList = "customer_id"), @Index(name = "idx_customer_otp_expiry", columnList = "expires_at")})
@Getter
@Setter
public class CustomerOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_otp_id")
    private Long customerOtpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, referencedColumnName = "customer_id")
    private CoCustomer customer;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "otp_reference_id")
    private String otpReferenceId;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "resend_count")
    private Integer resendCount;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}