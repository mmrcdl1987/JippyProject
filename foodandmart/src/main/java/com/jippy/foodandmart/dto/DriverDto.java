package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverDto {

    private Integer driverId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

    private Boolean isApproved;

    private Boolean readyToAcceptOrders;
}