package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_online_pricing",
        schema = "jippy_fm"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmProductOnlinePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_online_pricing_id")
    private Integer productOnlinePricingId;

    /**
     * Product to which this online price belongs.
     */
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /**
     * Identifies the outlet through outlet_categories.
     */
    @Column(name = "outlet_category_id", nullable = false)
    private Integer outletCategoryId;

    /**
     * NULL  -> Base product online price
     *
     * NOT NULL -> Variant/Add-on online price
     */
    @Column(name = "product_variant_id")
    private Integer productVariantId;

    /**
     * Final calculated online selling price.
     */
    @Column(
            name = "online_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal onlinePrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}