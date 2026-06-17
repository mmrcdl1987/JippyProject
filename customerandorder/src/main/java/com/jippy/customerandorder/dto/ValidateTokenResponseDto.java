package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class ValidateTokenResponseDto {

    private boolean valid;

    private Long customerId;

    private String mobileNumber;
}