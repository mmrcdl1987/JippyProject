package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "referrals", schema = "jippy_customer_and_order")
public class CoCustomerReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referral_id")
    private Integer referralId;

    @Column(name = "referrer_customer_id", nullable = false)
    private Integer referrerCustomerId;

    @Column(name = "referee_customer_id", nullable = false)
    private Integer refereeCustomerId;

    @Column(name = "referral_code", nullable = false)
    private String referralCode;

    @Column(name = "referral_status", nullable = false)
    private String referralStatus; // pending, qualified, rewarded

    @Column(name = "referral_type", nullable = false)
    private String referralType; // customer, driver

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}