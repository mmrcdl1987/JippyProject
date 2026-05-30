package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoFinalRejectRequestDto {

    private String orderId;

    private Long driverId;

    private String reason;

    private Double latitude;

    private Double longitude;
}