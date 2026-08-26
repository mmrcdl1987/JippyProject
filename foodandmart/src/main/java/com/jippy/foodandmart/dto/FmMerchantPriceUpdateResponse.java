package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmMerchantPriceUpdateResponse {

    private boolean success;

    private String message;

    private Integer productId;

    private Integer outletId;

    private BigDecimal oldPrice;

    private BigDecimal requestedPrice;

    private BigDecimal updatedPrice;

    private String role;

    private Integer updatedBy;

    private boolean priceUpdated;
}