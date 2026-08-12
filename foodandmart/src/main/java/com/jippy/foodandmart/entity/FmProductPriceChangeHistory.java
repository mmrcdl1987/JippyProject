package com.jippy.foodandmart.entity;

import com.jippy.foodandmart.enums.FmPriceHistoryOperationType;
import com.jippy.foodandmart.enums.FmPriceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_price_change_history", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
public class FmProductPriceChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_price_change_history_id")
    private Integer productPriceChangeHistoryId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /**
     * NULL = main product price
     * Non-null = variant-specific price
     */
    @Column(name = "product_variant_id")
    private Integer productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private FmPriceType priceType;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "old_price", precision = 12, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 12, scale = 2)
    private BigDecimal newPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 20)
    private FmPriceHistoryOperationType operationType;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "location_type")
    private String locationType;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}