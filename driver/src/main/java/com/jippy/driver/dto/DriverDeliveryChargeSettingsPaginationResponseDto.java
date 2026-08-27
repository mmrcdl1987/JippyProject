package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DriverDeliveryChargeSettingsPaginationResponseDto {

    private List<DriverDeliveryChargeSettingsGetAllResponseDto> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;
}