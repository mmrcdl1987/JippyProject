package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.FmPriceAdjustmentType;
import com.jippy.foodandmart.enums.FmPriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FmProductPriceSettingsRequestDto {

    @NotNull(message = "Outlet id is required")
    @Positive(message = "Outlet id must be greater than zero")
    private Integer outletId;

    @NotNull(message = "Product id is required")
    @Positive(message = "Product id must be greater than zero")
    private Integer productId;

    /**
     * NULL means main product price.
     * Non-null means variant-specific price.
     */
    @Positive(message = "Product variant id must be greater than zero")
    private Integer productVariantId;

    @NotNull(message = "Start date time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    private LocalDateTime endDateTime;

    @NotNull(message = "Price value is required")
    @DecimalMin(value = "0.01", message = "Price value must be greater than zero")
    private BigDecimal priceValue;

    @NotNull(message = "Price type is required")
    private FmPriceType priceType;

    @NotNull(message = "Price adjustment type is required")
    private FmPriceAdjustmentType priceAdjustmentType;

    @Positive(message = "Location id must be greater than zero")
    private Integer locationId;

    private String locationType;
}