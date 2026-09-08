package com.jippy.driver.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO containing driver details.
 */
@Getter
@Setter
public class DriverDetailsResponseDto {

    private Integer driverId;

    private String driverName;

    private String driverMobileNumber;
}