package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons" ,schema = "jippy_division")
@Data
public class DivCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private  Integer couponId;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "application_type")
    private Integer applicationType;

    @Column(name = "price_model_id")
    private Integer priceModelId;

    @Column(name = "min_order_value")
    private Double minOrderValue;

    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "payment_method")
    private Integer paymentMethod;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

}
