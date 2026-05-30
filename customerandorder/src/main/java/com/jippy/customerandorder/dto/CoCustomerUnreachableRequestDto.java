package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerUnreachableRequestDto {

    private String orderId;

    private Long driverId;

    private Double latitude;

    private Double longitude;

    private String reason;
}