package com.jippy.foodandmart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FmAssignCancelledOrderResponseDto {

    private Boolean success;

    private String message;
}