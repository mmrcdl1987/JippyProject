package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoOrderDto {

    private String orderId;

    private Integer driverId;

    private String orderStatus;

    private Integer paymentModeId;
}