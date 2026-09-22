package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO for merchant settlement details
 * for a specific outlet within the settlement period.
 */
@Getter
@Setter
public class FmMerchantSettlementForOutletResponseDto {

    /**
     * Outlet for which settlement was calculated.
     */
    private Integer outletId;

    /**
     * Number of delivered orders for the outlet.
     */
    private Long orderCount;

    /**
     * Total merchant amount from delivered orders.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Total merchant promotion amount deducted.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Merchant amount after promotion deduction.
     *
     * merchantTotalPrice - promotionDeductedAmount
     */
    private BigDecimal amountAfterPromotion;

    /**
     * Whether GST is applicable for the outlet.
     */
    private Boolean gstApplied;

    /**
     * GST percentage applied.
     */
    private BigDecimal gstPercentage;

    /**
     * GST amount deducted from amountAfterPromotion.
     */
    private BigDecimal gstDeductedAmount;

    /**
     * Final settlement amount after GST deduction
     * from the promotion-adjusted merchant amount.
     */
    private BigDecimal gstDeductedAmountFromPromotionAmount;
    /**
     * Final settlement amount after GST deduction.
     *
     * amountAfterPromotion - gstDeductedAmount
     */
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}