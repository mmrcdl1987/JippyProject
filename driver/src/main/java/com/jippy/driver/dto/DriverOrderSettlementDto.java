package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverOrderSettlementDto {

    private String orderId;

    private BigDecimal pickUpCharges;

    private BigDecimal deliverCharges;

    private BigDecimal totalDeliveryFee;

    private BigDecimal surgeFee;

    private BigDecimal tips;
}