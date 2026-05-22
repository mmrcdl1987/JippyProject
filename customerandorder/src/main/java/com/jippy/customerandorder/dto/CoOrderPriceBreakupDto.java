package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoOrderPriceBreakupDto {

    private String orderId;

    private BigDecimal orderAmount;

    private BigDecimal platformFee;

    private BigDecimal deliveryFee;

    private BigDecimal surgeFee;

    private BigDecimal packagingFee;

    private BigDecimal gst;

    private BigDecimal orderTotalAmount;

    private BigDecimal couponDiscount;
}