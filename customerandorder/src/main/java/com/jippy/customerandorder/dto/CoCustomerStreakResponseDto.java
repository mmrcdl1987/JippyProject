package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerStreakResponseDto {

    private Boolean success;

    private String message;

    private Integer currentStreak;

    private Integer maxStreak;

    private Integer points;
}   