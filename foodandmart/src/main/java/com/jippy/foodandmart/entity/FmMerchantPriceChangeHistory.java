package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "merchant_price_change_history",
        schema = "jippy_fm"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmMerchantPriceChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_price_change_history_id")
    private Integer merchantPriceChangeHistoryId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_variant_options_id")
    private Integer productVariantOptionsId;

    @Column(
            name = "old_price",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal oldPrice;

    @Column(
            name = "new_price",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal newPrice;

    @Column(name = "price_updated_by", length = 30)
    private String priceUpdatedBy;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}