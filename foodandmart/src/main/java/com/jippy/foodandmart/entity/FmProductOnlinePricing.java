package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_online_pricing", schema = "jippy_fm")

@Data
public class FmProductOnlinePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productOnlinePricingId;

    private Integer productId;

    @Column(name = "outlet_category_id")
    private Integer outletCategoryId;

    private BigDecimal onlinePrice;

    private LocalDateTime createdAt;
    private Integer createdBy;

    private LocalDateTime updatedAt;
    private Integer updatedBy;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "approved_by")
    private Integer approvedBy;
}