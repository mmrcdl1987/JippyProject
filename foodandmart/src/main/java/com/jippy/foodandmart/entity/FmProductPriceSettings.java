package com.jippy.foodandmart.entity;

import com.jippy.foodandmart.enums.FmPriceAdjustmentType;
import com.jippy.foodandmart.enums.FmPriceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_price_settings", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
public class FmProductPriceSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_price_settings_id")
    private Integer productPriceSettingsId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    /**
     * NULL = main product price.
     * Non-null = variant-option-specific price.
     */
    @Column(name = "product_variant_id")
    private Integer productVariantId;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "price_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private FmPriceType priceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_adjustment")
    private FmPriceAdjustmentType priceAdjustmentType;

    @Column(name = "location_id", nullable = false)
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