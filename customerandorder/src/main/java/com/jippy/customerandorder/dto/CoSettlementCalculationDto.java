package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Contains settlement calculation data returned
 * from Customer & Order microservice to FM.
 */
@Getter
@Setter
public class CoSettlementCalculationDto {

    /**
     * Total merchant price from order_items.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Total merchant promotion discount.
     */
    private BigDecimal promotionDeductedAmount;
}