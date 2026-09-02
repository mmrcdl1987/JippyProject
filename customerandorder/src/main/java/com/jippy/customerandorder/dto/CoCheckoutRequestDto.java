package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCheckoutRequestDto {

    private Integer customerId;

    private Integer customerAddressId;

    private Integer outletId;

    private Integer couponId;

    private BigDecimal walletAmount;

    private BigDecimal couponDiscount;

    private BigDecimal deliveryTip;
}