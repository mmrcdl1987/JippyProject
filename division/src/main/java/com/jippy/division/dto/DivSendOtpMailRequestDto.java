package com.jippy.division.dto;

import lombok.Data;

@Data
public class DivSendOtpMailRequestDto {

    private String email;

    private String otp;
}