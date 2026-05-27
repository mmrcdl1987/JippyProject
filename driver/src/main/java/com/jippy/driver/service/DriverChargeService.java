package com.jippy.driver.service;

import com.jippy.driver.dto.DeliveryChargeCalculationRequestDto;
import com.jippy.driver.dto.DeliveryChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverChargeCalculationRequestDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;

public interface DriverChargeService {

    DriverChargeCalculationResponseDto calculateDriverCharge(DriverChargeCalculationRequestDto requestDto);

    DeliveryChargeCalculationResponseDto calculateDeliveryCharge(DeliveryChargeCalculationRequestDto requestDto);
}