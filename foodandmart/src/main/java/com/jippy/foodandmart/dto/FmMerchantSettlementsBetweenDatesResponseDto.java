//package com.jippy.foodandmart.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//
///**
// * Response DTO for merchant settlements
// * calculated between two dates.
// *
// * Contains merchant details and the final
// * settlement amount after GST and promotion deductions.
// */
//@Getter
//@Setter
//public class FmMerchantSettlementsBetweenDatesResponseDto {
//
//    /**
//     * Merchant name.
//     */
//    private String merchantName;
//
//    /**
//     * Merchant mobile/phone number.
//     */
//    private String mobileNo;
//
//    /**
//     * Merchant city name.
//     */
//    private String cityName;
//
//    /**
//     * Number of delivered orders.
//     */
//    private Long count;
//
//    /**
//     * Total merchant price from order_items.
//     */
//    private BigDecimal merchantTotalPrice;
//
//    /**
//     * Total promotion amount deducted.
//     */
//    private BigDecimal promotionDeductedAmount;
//
//    /**
//     * Indicates whether GST is applicable.
//     */
//    private Boolean gstApplied;
//
//    /**
//     * GST percentage applied.
//     * Currently 5% when GST is applicable.
//     */
//    private BigDecimal gstPercentage;
//
//    /**
//     * GST amount deducted from merchant total price.
//     */
//    private BigDecimal gstDeductedAmount;
//
//    /**
//     * Final settlement amount after
//     * GST and promotion deductions.
//     */
//    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
//}