package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "subscription_plan_id")
    private Integer subscriptionPlanId;

    @Column(name = "subscription_from_date")
    private LocalDate subscriptionFromDate;

    @Column(name = "subscription_to_date")
    private LocalDate subscriptionToDate;

    /*@Column(name = "banner_from_date")
    private LocalDate bannerFromDate;

    @Column(name = "banner_to_date")
    private LocalDate bannerToDate;*/

    @Column(name = "main_banner_url")
    private String mainBannerUrl;

    @Column(name = "best_restaurant_banner_url")
    private String bestRestaurantBannerUrl;

    @Column(name = "deals_banner_url")
    private String dealsBannerUrl;

    @Column(name = "price_model_type")
    private String priceModelType;

    @Column(name = "offer_amount")
    private BigDecimal offerAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}