package com.jippy.division.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DivCustomerResponseDto {

    private Integer customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Integer customerStatusId;
    private String referralCode;
    private LocalDate DOB;
    private String profilePicUrl;
}
