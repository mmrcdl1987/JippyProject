package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerResponseDto {
    private Integer customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer customerStatusId;
    private String referralCode;
}