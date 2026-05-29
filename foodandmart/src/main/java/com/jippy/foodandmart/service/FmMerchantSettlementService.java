package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmOutletsResponseDto;
import com.jippy.foodandmart.dto.FmProductResponseDto;

public interface FmMerchantSettlementService {
    FmProductResponseDto getProductById(Integer productId);

    FmOutletsResponseDto getOutletById(Integer outletId);

    FmAreaDto getAreaById(Integer areaId);
}
