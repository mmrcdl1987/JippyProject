package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverCustomerProfilePicDto {

    // Customer ID whose profile picture needs to be updated
    private Integer customerId;

    // New profile picture URL generated from S3
    private String profilePicUrl;
}