package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmUpdateCODResponseDto;

public interface FmFleetManagerService {
    FmUpdateCODResponseDto updateCODAmountByFleetManager(Integer driverId, Integer fleetManagerId);

}
