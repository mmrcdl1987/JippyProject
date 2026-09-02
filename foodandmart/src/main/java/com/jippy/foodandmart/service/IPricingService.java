package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmProduct;

import java.util.List;
import java.util.Optional;

public interface IPricingService {


    List<FmOutletDto> getOutlets(Integer areaId, boolean isApproved, String search);

    List<FmProductResponseDto> getProducts(List<Integer> outletIds, boolean isApproved);

    void updatePrices(FmPriceUpdateRequestDto dto, boolean isApproved);

    FmProductDetailResponseDto getProductById(Integer productId);

    FmProductDetailResponseDto getProductByIdAndOutletId(
            Integer productId,
            Integer outletId);

    void bulkUpdatePrices(
            FmBulkPriceUpdateRequestDto dto,
            boolean isApproved
    );

    List<FmCurrentOnlinePriceResponse> getCurrentOnlinePrices(
            FmCurrentOnlinePriceRequest request
    );
}

