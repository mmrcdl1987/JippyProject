package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.CreateOutletUnavailabilityRequestDto;
import com.jippy.foodandmart.dto.AvailabilityActionRequestDto;

public interface OutletUnavailabilityService {

    void createUnavailability(CreateOutletUnavailabilityRequestDto requestDto);
    void restoreAvailability(AvailabilityActionRequestDto requestDto);
}