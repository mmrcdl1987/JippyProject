package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverZoneAssignmentResponseDto {

    private Integer driverZoneAssignmentId;

    private Integer driverId;

    private Integer zoneId;

    private String message;
}