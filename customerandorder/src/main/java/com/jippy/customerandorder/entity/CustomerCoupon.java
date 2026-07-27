package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customer_coupons", schema = "jippy_customer_and_order")
public class CustomerCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_coupon_id")
    private Integer customerCouponId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "coupon_id", nullable = false)
    private Integer couponId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "is_redeemed")
    private Boolean isRedeemed = false;

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}