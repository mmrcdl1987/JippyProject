package com.jippy.foodandmart.dto;

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
    private List<FmProductVariantDTO> variants;
    private List<FmProductTimingDto> productTimings;
}
