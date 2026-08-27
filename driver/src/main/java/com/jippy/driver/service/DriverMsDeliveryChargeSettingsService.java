package com.jippy.driver.service;


import com.jippy.driver.dto.DriverDeliveryChargeSettingsDeleteRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetAllResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetByIdResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsPaginationResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsSaveRequestDto;

public interface DriverMsDeliveryChargeSettingsService {

    DriverDeliveryChargeSettingsGetByIdResponseDto save(DriverDeliveryChargeSettingsSaveRequestDto request);

    DriverDeliveryChargeSettingsGetByIdResponseDto getById(Integer deliveryChargeSettingId);

    DriverDeliveryChargeSettingsPaginationResponseDto getAll(int page, int size);

    void delete(DriverDeliveryChargeSettingsDeleteRequestDto request);
}