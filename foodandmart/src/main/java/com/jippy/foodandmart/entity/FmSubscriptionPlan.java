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

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must be less than 100 characters")
    @Column(name = "plan_name", nullable = false)
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(name = "duration_in_days")
    private Integer durationInDays;

    @NotNull(message = "Radius is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Radius must be greater than 0")
    @Column(name = "radius_in_kms")
    private BigDecimal radiusInKms;

    @Size(max = 50)
    @Column(name = "banner_slots")
    private String bannerSlots;

    @Column(name = "banner_days")
    private Integer bannerDays;

    @Size(max = 50)
    @Column(name = "best_restaurant_slot")
    private String bestRestaurantSlot;

    @Size(max = 50)
    @Column(name = "whatsapp_broadcast")
    private String whatsappBroadcast;

    @Size(max = 50, message = "Video credits must not exceed 50 characters")
    @Column(name = "video_credits")
    private String videoCredits;

    @NotNull(message = "State id is required")
    @Column(name = "state_id")
    private Integer stateId;

    @NotNull(message = "City id is required")
    @Column(name = "city_id")
    private Integer cityId;

    @NotNull(message = "Area id is required")
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