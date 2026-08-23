package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoWalletSettingsRequestDto {

    private Integer walletSettingsId;

    private String settingType;

    private Integer settingValue;

    private Integer createdBy;

    private Integer updatedBy;
}