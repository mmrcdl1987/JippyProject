package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoCheckoutResponseDto {

    private List<CoCartItemResponseDto> items;

    private BigDecimal itemTotal;

    private BigDecimal deliveryCharge;

    private BigDecimal platformFee;

    private BigDecimal surgeFee;

    private BigDecimal packagingFee;

    private BigDecimal foodTax;

    private BigDecimal deliveryTax;

    private BigDecimal taxesAndCharges;

    private BigDecimal couponDiscount;

    private BigDecimal deliveryTip;

    private BigDecimal toPay;

    private Boolean codAvailable;

    private String message;
}