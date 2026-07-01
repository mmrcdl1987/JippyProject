package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmProductDto {
    private Integer    productId;
    private String     productName;
    private String     description;
    private BigDecimal merchantPrice;
    private BigDecimal Price;
    private Boolean    isVeg;
    private Boolean    hasProductVariants;
    // Outlet Category Toggle
    @Schema(example = "true")
    private Boolean isAvailable;

    // Product favourite status for logged-in customer
    @Schema(example = "true",
            description = "Indicates whether this product is marked as favourite by the customer.")
    private Boolean isProductFavourite;

    private List<FmProductVariantDTO> variants;
    private List<FmProductTimingDto> productTimings;
}
