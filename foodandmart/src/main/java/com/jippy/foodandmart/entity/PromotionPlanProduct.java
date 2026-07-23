package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promotion_plan_products", schema = "jippy_fm")
public class PromotionPlanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_plan_products_id")
    private Integer promotionPlanProductsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_plans_id", nullable = false)
    private PromotionPlan promotionPlan;

    @Column(name = "outlet_category_id")
    private Integer outletCategoryId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}