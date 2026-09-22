package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for outlet and order settlement details
 * calculated for the selected settlement period.
 */
@Getter
@Setter
public class FmOutletAndOrdersCountForSettlementResponseDto {

    /**
     * Settlement period start date.
     */
    private LocalDate startDate;

    /**
     * Settlement period end date.
     */
    private LocalDate endDate;

    /**
     * Unique outlet IDs having delivered orders
     * during the selected settlement period.
     */
    private List<Integer> outletIds;

    private List<FmOutletSettlementDto> outletSettlements;

    /**
     * Number of unique outlets having delivered orders
     * during the selected settlement period.
     */
    private Integer outletsCountsBetweenDates;

    /**
     * Number of delivered orders during the
     * selected settlement period.
     */
    private Integer orderCountsBetweenDates;

    /**
     * Sum of merchant_total_price from order_items
     * for all delivered orders in the selected period.
     */
    private BigDecimal merchantTotalPriceBetweenDates;

    /**
     * Total promotion amount deducted from the
     * delivered orders during the selected period.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Indicates whether GST is applicable.
     */
    private Boolean gstApplied;

    /**
     * GST amount deducted from the merchant total price.
     */
    private BigDecimal gstDeductedAmount;

    /**
     * Final settlement amount after deducting
     * promotion and GST amounts.
     */
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}