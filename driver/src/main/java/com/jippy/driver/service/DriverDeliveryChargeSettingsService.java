package com.jippy.driver.service;


import com.jippy.driver.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsResponseDto;

public interface DriverDeliveryChargeSettingsService {
    DriverDeliveryChargeSettingsResponseDto createDriverDeliveryChargeSetting(DriverDeliveryChargeSettingsRequestDto requestDto);
}
