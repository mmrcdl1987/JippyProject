package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FmProductResponseDto {
    private Integer productId;
    private String productName;
    private BigDecimal merchantPrice;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal onlinePrice;
}