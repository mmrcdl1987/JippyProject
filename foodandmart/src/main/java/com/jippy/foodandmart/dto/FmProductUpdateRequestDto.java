package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmProductUpdateRequestDto {

    @NotBlank(message = "Product Name is required")
    private String productName;

    private String description;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private BigDecimal merchantPrice;

    private String imageLink;

    private String photos;

    private String thumbnail;

    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Merchant Variant Groups with Variant Prices
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}