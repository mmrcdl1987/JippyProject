//package com.jippy.foodandmart.dto;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
///**
// * Response DTO representing settlement details
// * for an individual order item.
// *
// * One order can contain multiple products.
// * Therefore, one order can produce multiple
// * records of this DTO.
// */
//@Getter
//@Setter
//public class FmMerchantSettlementOrderItemResponseDto {
//
//    /**
//     * Order ID fetched from CO orders table.
//     */
//    @Schema(
//            description = "Unique order ID.",
//            example = "jippy202609193"
//    )
//    private String orderId;
//
//    /**
//     * Order creation timestamp fetched from CO orders table.
//     */
//    @Schema(
//            description = "Order creation timestamp.",
//            example = "2026-09-19T15:30:54.507314"
//    )
//    private LocalDateTime createdAt;
//
//    /**
//     * Product name fetched from FM products table.
//     */
//    @Schema(
//            description = "Product name.",
//            example = "Butter Chicken"
//    )
//    private String productName;
//
//    /**
//     * Variant name fetched through the FM variant tables.
//     */
//    @Schema(
//            description = "Product variant name.",
//            example = "Regular"
//    )
//    private String variantName;
//
//    /**
//     * Quantity ordered.
//     */
//    @Schema(
//            description = "Quantity of the product ordered.",
//            example = "2"
//    )
//    private Integer quantity;
//
//    /**
//     * Merchant total price fetched from
//     * CO order_items.merchant_total_price.
//     */
//    @Schema(
//            description = "Merchant total price before promotion deduction.",
//            example = "758.00"
//    )
//    private BigDecimal merchantTotalPrice;
//
//    /**
//     * Amount deducted due to merchant promotion.
//     *
//     * This is applicable when the order price breakup
//     * has discount_type = MERCHANT_PROMOTION.
//     */
//    @Schema(
//            description = "Amount deducted due to merchant promotion.",
//            example = "0.00"
//    )
//    private BigDecimal promotionDeductedAmount;
//
//    /**
//     * Merchant amount after deducting promotion.
//     *
//     * Calculation:
//     *
//     * merchantTotalPrice - promotionDeductedAmount
//     */
//    @Schema(
//            description = "Merchant amount after promotion deduction.",
//            example = "758.00"
//    )
//    private BigDecimal amountAfterPromotion;
//
//    /**
//     * Indicates whether GST is applicable for the outlet.
//     *
//     * Value comes from FM outlets.is_gst_applied.
//     */
//    @Schema(
//            description = "Indicates whether GST is applicable for the outlet.",
//            example = "true"
//    )
//    private Boolean gstApplied;
//
//    /**
//     * GST percentage applied.
//     *
//     * Current requirement uses 5%.
//     */
//    @Schema(
//            description = "GST percentage applied to the amount after promotion.",
//            example = "5"
//    )
//    private BigDecimal gstPercentage;
//
//    /**
//     * GST amount calculated on amountAfterPromotion.
//     *
//     * Calculation:
//     *
//     * amountAfterPromotion * gstPercentage / 100
//     */
//    @Schema(
//            description = "GST amount deducted from the amount after promotion.",
//            example = "37.9000"
//    )
//    private BigDecimal gstDeductedAmount;
//
//    /**
//     * Final amount after promotion and GST deductions.
//     *
//     * Calculation:
//     *
//     * amountAfterPromotion - gstDeductedAmount
//     */
//    @Schema(
//            description = "Final amount after promotion and GST deduction.",
//            example = "720.1000"
//    )
//    private BigDecimal gstDeductedAmountFromPromotionAmount;
//
//    /**
//     * Net settlement amount for this individual
//     * order item.
//     *
//     * For the current requirement, this value is the
//     * same as gstDeductedAmountFromPromotionAmount.
//     */
//    @Schema(
//            description = "Net settlement amount after promotion and GST deductions for this order item.",
//            example = "720.1000"
//    )
//    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
//}