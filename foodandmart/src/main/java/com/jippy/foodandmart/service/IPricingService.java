package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmBulkPriceUpdateRequestDto;
import com.jippy.foodandmart.dto.FmOutletDto;
import com.jippy.foodandmart.dto.FmPriceUpdateRequestDto;
import com.jippy.foodandmart.dto.FmProductResponseDto;

import java.util.List;
public interface IPricingService {


    List<FmOutletDto> getOutlets(Integer areaId, boolean isApproved, String search);

    List<FmProductResponseDto> getProducts(List<Integer> outletIds, boolean isApproved);

    void updatePrices(FmPriceUpdateRequestDto dto, boolean isApproved);

    void bulkUpdatePrices(FmBulkPriceUpdateRequestDto dto, boolean isApproved);
}

