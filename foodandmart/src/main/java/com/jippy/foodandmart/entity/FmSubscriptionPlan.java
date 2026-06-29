package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans", schema = "jippy_fm")
@Data
public class FmSubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_plan_id")
    private Integer subscriptionPlanId;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "duration_in_days")
    private Integer durationInDays;

    @Column(name = "banner_duration_in_days")
    private Integer bannerDurationInDays;

    @Column(name = "radius_in_kms")
    private BigDecimal radiusInKms;

    @Column(name = "banner_slot")
    private Integer bannerSlot;

    @Column(name = "best_restaurant_slot")
    private Integer bestRestaurantSlot;

    @Column(name = "deals_slot")
    private Integer dealsSlot;

    @Column(name = "whatsapp_broadcast")
    private String whatsappBroadcast;

    @Column(name = "video_credits")
    private String videoCredits;

 /*   @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "city_id")
    private Integer cityId;*/

    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}