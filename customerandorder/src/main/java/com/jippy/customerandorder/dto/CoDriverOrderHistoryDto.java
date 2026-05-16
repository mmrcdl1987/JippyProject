package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoDriverOrderHistoryDto {

    // Driver id
    private Integer driverId;

    // Order id
    private Integer orderId;

    // Pick up distance
    private BigDecimal pickUpDistanceInKms;

    // Delivery distance
    private BigDecimal deliveryDistanceInKms;

    // Pick up charges
    private BigDecimal pickUpCharges;

    // Delivery charges
    private BigDecimal deliverCharges;

    // Total delivery fee
    private BigDecimal totalDeliveryFee;

    // Surge fee
    private BigDecimal surgeFee;

    // Tips
    private BigDecimal tips;

    // Order status from orders table
    private String orderStatus;

    // Outlet name from FM microservice
    private String outletName;
}