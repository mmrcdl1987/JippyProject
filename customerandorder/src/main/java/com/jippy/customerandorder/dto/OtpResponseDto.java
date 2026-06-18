package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class OtpResponseDto {

    private boolean success;

    private String message;
}