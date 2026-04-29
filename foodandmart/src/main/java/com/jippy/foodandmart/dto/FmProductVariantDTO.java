package com.jippy.foodandmart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmProductVariantDTO {
    private Integer variantId;
    private String variantName;
    private BigDecimal merchantPrice;
    private BigDecimal price;



}
