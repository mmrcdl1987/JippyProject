package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriveOrderPriceBreakupDto {

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