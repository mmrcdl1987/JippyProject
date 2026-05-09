package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmNearbyOutletResponseDto;

public interface FmSpecializedOutletService {

    FmNearbyOutletResponseDto
    fetchSpecializedOutletsByAreaId(
            Integer areaId
    );
}