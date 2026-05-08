package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoDriverZoneAssignmentResponseDto {

    private Integer driverZoneAssignmentId;

    private Integer driverId;

    private Integer zoneId;

    private String message;
}