package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for settlement order calculations.
 *
 * This DTO is returned from Customer and Order (CO)
 * to Food and Mart (FM).
 *
 * CO is responsible for order-related calculations.
 *
 * FM will use this data to calculate:
 * - GST
 * - GST deduction
 * - Final settlement amount
 */
@Getter
@Setter
public class CoOutletAndOrdersSettlementResponseDto {

    /**
     * Unique outlet IDs having delivered orders
     * during the selected settlement period.
     */
    private List<Integer> outletIds;

    /**
     * Outlet-wise settlement details.
     *
     * Contains merchant amount and promotion deduction
     * separately for each outlet.
     */
    private List<CoOutletSettlementDto> outletSettlements;

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
     * for all delivered orders during the selected period.
     */
    private BigDecimal merchantTotalPriceBetweenDates;

    /**
     * Total promotion discount deducted from
     * delivered orders.
     *
     * This is calculated from order_price_breakup.discount
     * where discount_type = 'MERCHANT_PROMOTION'.
     */
    private BigDecimal promotionDeductedAmount;
}