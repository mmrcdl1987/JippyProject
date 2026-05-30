package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmAssignCancelledOrderRequestDto {

    private String orderId;

    private Integer driverId;

    private Integer specializedOutletId;
}