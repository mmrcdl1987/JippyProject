package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriveOrderDto {

    private String orderId;

    private Integer driverId;

    private String orderStatus;

    private Integer paymentModeId;

    // ADD THIS
    private Integer outletId;
}