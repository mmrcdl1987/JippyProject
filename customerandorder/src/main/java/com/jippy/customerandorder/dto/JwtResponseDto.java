package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class JwtResponseDto {

    private Long customerId;

    private String mobileNumber;

    private String firstName;

    private String lastName;

    private String accessToken;

    private String tokenType = "Bearer";

    private Long expiresIn;
}