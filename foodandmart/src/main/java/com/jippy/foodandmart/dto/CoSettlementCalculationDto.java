package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO used by FM to receive settlement
 * calculation details from CO microservice.
 */
@Getter
@Setter
public class CoSettlementCalculationDto {

    /**
     * Sum of merchant_total_price from
     * order_items for the outlet.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Total discount amount where
     * discount_type = MERCHANT_PROMOTION.
     */
    private BigDecimal promotionDeductedAmount;
}