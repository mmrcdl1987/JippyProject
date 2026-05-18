package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class CustomerResponseDto {

    private Integer customerId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}