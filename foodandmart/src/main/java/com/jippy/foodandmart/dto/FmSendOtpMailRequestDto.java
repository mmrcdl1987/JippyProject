package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
//data from DIV microservice
public class FmSendOtpMailRequestDto {

    private String email;

    private String otp;
}