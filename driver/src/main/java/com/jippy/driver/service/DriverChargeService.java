package com.jippy.driver.service;

import com.jippy.driver.dto.*;
import org.springframework.data.domain.Pageable;

public interface DriverChargeService {

    // =========================================================
    // DRIVER PAYOUT CALCULATION
    // =========================================================

    DriverChargeCalculationResponseDto calculateDriverCharge(DriverChargeCalculationRequestDto requestDto);


    // =========================================================
    // CHECKOUT DELIVERY CHARGE
    // =========================================================

    DeliveryChargeCalculationResponseDto calculateDeliveryCharge(DeliveryChargeCalculationRequestDto requestDto);


    // =========================================================
    // GET DELIVERY CHARGE SETTINGS
    // STATE + CITY + AREA + PAGINATION
    // =========================================================

    //DriverDeliveryChargeSettingsPageResponseDto getDeliveryChargeSettings(Pageable pageable);
}