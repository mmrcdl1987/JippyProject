package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoOrderPriceBreakupDto {

    private String orderId;

    // ================= ORDER =================

    private BigDecimal orderAmount;

    private BigDecimal orderAmountDiscounted;

    // ================= DELIVERY =================

    private BigDecimal pickUpDistanceKms;

    private BigDecimal deliveryDistanceKms;

    private BigDecimal pickUpCharges;

    private BigDecimal driverDeliveryFee;

    private BigDecimal customerDeliveryFee;

    private BigDecimal totalDeliveryFee;

    private BigDecimal customerDeliveryFeeTax;

    // ================= PLATFORM =================

    private BigDecimal platformFee;

    private BigDecimal platformFeeTax;

    // ================= SURGE =================

    private BigDecimal surgeFee;

    private BigDecimal surgeFeeTax;

    // ================= PACKAGING =================

    private BigDecimal packagingFee;

    private BigDecimal packagingFeeTax;

    // ================= TAX =================

    private BigDecimal foodTax;

    private BigDecimal totalTax;

    // ================= PAYMENT / DISCOUNT =================

    private BigDecimal tip;

    private BigDecimal couponDiscount;

    private BigDecimal walletAmount;

    // ================= FINAL =================

    private BigDecimal orderTotalAmount;
}