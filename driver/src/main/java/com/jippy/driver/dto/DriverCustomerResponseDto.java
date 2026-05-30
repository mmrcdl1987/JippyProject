package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverCustomerResponseDto {
    private Integer customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer customerStatusId;
    private String referralCode;
    private String profilePicUrl;
    private Integer createdBy;
}