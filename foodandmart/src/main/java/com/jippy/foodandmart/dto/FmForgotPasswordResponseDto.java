package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FmForgotPasswordResponseDto {

    private Boolean status;

    private String message;
}