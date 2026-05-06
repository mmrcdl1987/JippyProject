package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FmProductDetailResponseDto {
    private Integer productId;
    private Integer outletCategoryId;
    private String productName;
    private String description;
    private BigDecimal merchantPrice;
    private Boolean isVeg;
    private Boolean hasProductVariants;
    private String imageLink;
    private String photos;
    private String thumbnail;
}