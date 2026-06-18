package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "outlet_subscription_plans", schema = "jippy_fm")
@Data
public class FmOutletSubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_subscription_plan_id")
    private Integer outletSubscriptionPlanId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "subscription_plan_id", nullable = false)
    private Integer subscriptionPlanId;

    @Column(name = "subscription_from_date", nullable = false)
    private LocalDate subscriptionFromDate;

    @Column(name = "subscription_to_date", nullable = false)
    private LocalDate subscriptionToDate;

    @Column(name = "banner_from_date")
    private LocalDate bannerFromDate;

    @Column(name = "banner_to_date")
    private LocalDate bannerToDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}