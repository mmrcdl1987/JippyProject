package com.jippy.foodandmart.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendOtpResponseDto {

    private String message;

    private Long expiresInMinutes;
}