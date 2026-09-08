package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents driver information received from Driver microservice.
 */
@Getter
@Setter
public class CoDriverDetailsDto {

    // Driver ID assigned to this order
    private Integer driverId;

    // Driver full name
    private String driverName;

    // Driver mobile number
    private String driverMobileNumber;
}