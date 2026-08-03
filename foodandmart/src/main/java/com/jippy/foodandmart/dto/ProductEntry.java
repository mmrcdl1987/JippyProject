package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductEntry {

    private Integer masterProductId;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;

    private Boolean isVeg;

    /**
     * If false:
     * Merchant price only
     *
     * If true:
     * Use variantGroups
     */
    private Boolean hasProductVariants;

    /**
     * Used only when hasProductVariants = false
     */
    private BigDecimal merchantPrice;

    private String imageLink;

    private String csvTiming;

    private String csvDayOfWeek;

    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Used only when hasProductVariants = true
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}