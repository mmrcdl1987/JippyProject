package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoWalletSettingsResponseDto {

    private Integer walletSettingsId;
    private String pointsType;
    private Integer numOfPoints;
    private Integer streakMinDays;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}