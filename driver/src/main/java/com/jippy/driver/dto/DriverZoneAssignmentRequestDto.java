package com.jippy.driver.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverZoneAssignmentRequestDto {

    // Driver id should not be null
    @NotNull(message = "Driver id is required")
    private Integer driverId;

    // Latitude validation
//    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0",
            message = "Latitude should be greater than or equal to -90")
    @DecimalMax(value = "90.0",
            message = "Latitude should be less than or equal to 90")
    private Double latitude;

    // Longitude validation
//    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0",
            message = "Longitude should be greater than or equal to -180")
    @DecimalMax(value = "180.0",
            message = "Longitude should be less than or equal to 180")
    private Double longitude;

    private Integer zoneId;
}