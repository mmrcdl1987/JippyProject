package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoWalletSettingsRequestDto {

    private Integer walletSettingsId;

    private String pointsType;

    private Integer numOfPoints;

    private Integer streakMinDays;

    private Integer createdBy;

    private Integer updatedBy;
}