package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoCheckoutResponseDto {

    private Integer outletId;

    private List<CoCartItemResponseDto> items;

    private BigDecimal itemTotal;

    private BigDecimal deliveryCharge;

    // ================= PLATFORM FEE =================

    private BigDecimal platformFee;

    private BigDecimal platformFeeTax;

    private Boolean platformFeeToggle;

    // ================= SURGE FEE =================

    private BigDecimal surgeFee;

    private BigDecimal surgeFeeTax;

    private Boolean surgeFeeToggle;

    // ================= PACKAGING FEE =================

    private BigDecimal packagingFee;

    private BigDecimal packagingFeeTax;

    private Boolean packagingFeeToggle;

    // ================= TAXES =================

    private BigDecimal foodTax;

    private BigDecimal deliveryTax;

    private BigDecimal taxesAndCharges;

    // ================= DISCOUNT / TIP =================

    private BigDecimal couponDiscount;

    private BigDecimal deliveryTip;

    // ================= FINAL =================

    private BigDecimal toPay;

    private Boolean codAvailable;
}