package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverTotalEarningsDto {

    private Integer driverId;

    private BigDecimal totalPickUpCharges;

    private BigDecimal totalDeliveryCharges;

    private BigDecimal totalTips;

    private BigDecimal totalSurgeFee;

    private BigDecimal totalEarnings;

    private Long completedOrders;

    private Long rejectedOrders;

    private Long totalOrders;
}