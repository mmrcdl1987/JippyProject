package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.FmPriceAdjustmentType;
import com.jippy.foodandmart.enums.FmPriceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmProductPriceSettingsResponseDto {

    private Integer productPriceSettingsId;

    private Integer outletId;

    private Integer productId;

    private Integer productVariantId;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private BigDecimal priceValue;

    private FmPriceType priceType;

    private FmPriceAdjustmentType priceAdjustmentType;

    private Integer locationId;

    private String locationType;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;
}