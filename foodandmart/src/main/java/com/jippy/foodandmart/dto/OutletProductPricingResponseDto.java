package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutletProductPricingResponseDto {

    private Integer outletId;

    private List<OutletProductPricingDto> products;
}
