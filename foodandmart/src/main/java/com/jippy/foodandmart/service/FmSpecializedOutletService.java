package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmNearbyOutletResponseDto;
import com.jippy.foodandmart.dto.FmPublicNearbyOutletResponseDto;

public interface FmSpecializedOutletService {

    FmNearbyOutletResponseDto fetchSpecializedOutletsByAreaId(Integer areaId);

    FmNearbyOutletResponseDto fetchNearbySpecializedOutlets(Double latitude, Double longitude);

    FmPublicNearbyOutletResponseDto fetchPublicNearbySpecializedOutlets(Double latitude, Double longitude, Integer areaId);
}
