package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CO response DTO representing settlement details
 * for one order item.
 *
 * This DTO contains the complete item-level structure
 * required by the CO settlement API.
 *
 * CO provides the order and pricing information.
 * FM-specific information such as product name, variant name,
 * GST applicability and GST percentage will be obtained
 * through the FM Feign client.
 */
@Getter
@Setter
public class CoMerchantSettlementOrderItemResponseDto {

    /**
     * Order ID from CO orders table.
     */
    @Schema(
            description = "Unique order ID.",
            example = "jippy202609193"
    )
    private String orderId;

    /**
     * Order creation timestamp from CO orders table.
     */
    @Schema(
            description = "Order creation timestamp.",
            example = "2026-09-19T15:30:54.507314"
    )
    private LocalDateTime createdAt;

    /**
     * Product ID from CO order_items table.
     *
     * This ID is sent to FM to fetch the product name.
     */
    @Schema(
            description = "Product ID.",
            example = "10"
    )
    private Integer productId;

    /**
     * Variant option ID from CO order_items table.
     *
     * This ID is sent to FM to fetch the variant name.
     */
    @Schema(
            description = "Product variant option ID.",
            example = "5"
    )
    private Integer variantOptionId;

    /**
     * Product name fetched from FM.
     */
    @Schema(
            description = "Product name.",
            example = "Butter Chicken"
    )
    private String productName;

    /**
     * Variant name fetched from FM.
     */
    @Schema(
            description = "Product variant name.",
            example = "Regular"
    )
    private String variantName;

    /**
     * Quantity ordered from CO order_items table.
     */
    @Schema(
            description = "Quantity of the product ordered.",
            example = "2"
    )
    private Integer quantity;

    /**
     * Merchant total price from CO order_items table.
     *
     * This is before merchant promotion deduction.
     */
    @Schema(
            description = "Merchant total price before promotion deduction.",
            example = "758.00"
    )
    private BigDecimal merchantTotalPrice;

    /**
     * Merchant promotion deduction calculated from
     * CO order_price_breakup.
     */
    @Schema(
            description = "Amount deducted due to merchant promotion.",
            example = "0.00"
    )
    private BigDecimal promotionDeductedAmount;

    /**
     * Merchant amount after promotion deduction.
     *
     * Calculation:
     *
     * merchantTotalPrice - promotionDeductedAmount
     */
    @Schema(
            description = "Merchant amount after promotion deduction.",
            example = "758.00"
    )
    private BigDecimal amountAfterPromotion;

    /**
     * GST applicability fetched from FM outlet configuration.
     */
    @Schema(
            description = "Indicates whether GST is applied for the outlet.",
            example = "true"
    )
    private Boolean gstApplied;

    /**
     * GST percentage fetched from FM.
     */
    @Schema(
            description = "GST percentage applied.",
            example = "5"
    )
    private BigDecimal gstPercentage;

    /**
     * GST amount calculated by the settlement logic.
     *
     * Calculation:
     *
     * amountAfterPromotion * gstPercentage / 100
     */
    @Schema(
            description = "GST amount deducted from the amount after promotion.",
            example = "37.9000"
    )
    private BigDecimal gstDeductedAmount;

    /**
     * Final amount after promotion and GST deductions.
     *
     * Calculation:
     *
     * amountAfterPromotion - gstDeductedAmount
     */
    @Schema(
            description = "Final settlement amount after promotion and GST deductions.",
            example = "720.1000"
    )
    private BigDecimal gstDeductedAmountFromPromotionAmount;

    /**
     * Final net settlement amount for this individual
     * order item.
     *
     * For the current requirement, this is the same as
     * gstDeductedAmountFromPromotionAmount.
     */
    @Schema(
            description = "Net settlement amount after promotion and GST deductions for this order item.",
            example = "720.1000"
    )
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}