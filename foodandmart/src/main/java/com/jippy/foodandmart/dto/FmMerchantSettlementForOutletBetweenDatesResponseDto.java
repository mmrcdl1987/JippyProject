package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO containing merchant settlement details
 * for a specific outlet within the requested settlement period.
 */
@Getter
@Setter
public class FmMerchantSettlementForOutletBetweenDatesResponseDto {

    /**
     * Outlet ID for which settlement was calculated.
     */
    @Schema(description = "Outlet ID", example = "3")
    private Integer outletId;

    /**
     * Number of delivered orders during the settlement period.
     */
    @Schema(description = "Total number of delivered orders", example = "6")
    private Long orderCount;

    /**
     * Total merchant amount from delivered orders.
     */
    @Schema(description = "Total merchant price before promotion deduction", example = "2689.00")
    private BigDecimal merchantTotalPrice;

    /**
     * Total merchant promotion amount deducted
     * from the settlement amount.
     */
    @Schema(description = "Total merchant promotion deduction", example = "70.00")
    private BigDecimal promotionDeductedAmount;

    /**
     * Merchant amount remaining after promotion deduction.
     *
     * Calculation:
     * merchantTotalPrice - promotionDeductedAmount
     */
    @Schema(description = "Amount remaining after promotion deduction", example = "2619.00")
    private BigDecimal amountAfterPromotion;

    /**
     * Indicates whether GST is applicable
     * for the outlet.
     */
    @Schema(description = "Whether GST is applicable for the outlet", example = "true")
    private Boolean gstApplied;

    /**
     * GST percentage applicable to the outlet.
     */
    @Schema(description = "GST percentage", example = "5")
    private BigDecimal gstPercentage;

    /**
     * GST amount deducted from the amount after promotion.
     */
    @Schema(description = "GST deducted from the settlement amount", example = "130.95")
    private BigDecimal gstDeductedAmount;

    /**
     * Amount after promotion deduction and GST deduction.
     *
     * Calculation:
     * amountAfterPromotion - gstDeductedAmount
     */
    @Schema(
            description = "Final amount after promotion and GST deductions",
            example = "2488.05"
    )
    private BigDecimal gstDeductedAmountFromPromotionAmount;

    /**
     * Final net settlement amount payable to the merchant.
     */
    @Schema(
            description = "Final net settlement amount after all deductions",
            example = "2488.05"
    )
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}