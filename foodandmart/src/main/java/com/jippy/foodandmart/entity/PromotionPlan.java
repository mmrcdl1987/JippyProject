package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "promotion_plans", schema = "jippy_fm")
public class PromotionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_plans_id")
    private Integer promotionPlanId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_plan_types_id", nullable = false)
    private PromotionPlanType promotionPlanType;

    @Column(name = "plan_start_date")
    private LocalDate planStartDate;

    @Column(name = "plan_end_date")
    private LocalDate planEndDate;

    @Column(name = "plan_start_time")
    private LocalTime planStartTime;

    @Column(name = "plan_end_time")
    private LocalTime planEndTime;

    @Column(name = "offer_name", length = 200)
    private String offerName;

    @Column(name = "minimum_order_value")
    private BigDecimal minimumOrderValue;

    @Column(name = "offer_amount")
    private BigDecimal offerAmount;

    @Column(name = "offer_type", length = 50)
    private String offerType;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}