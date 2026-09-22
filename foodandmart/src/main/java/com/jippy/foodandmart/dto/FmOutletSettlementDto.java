package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FmOutletSettlementDto {

    /**
     * Outlet ID for which the settlement is calculated.
     */
    private Integer outletId;

    /**
     * Total merchant amount for this outlet.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Promotion deduction for this outlet.
     */
    private BigDecimal promotionDeductedAmount;
}