package com.jippy.customerandorder.dto;

import com.jippy.customerandorder.dto.CoCartItemResponseDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoCheckoutResponseDto {

    private Integer outletId;

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
}