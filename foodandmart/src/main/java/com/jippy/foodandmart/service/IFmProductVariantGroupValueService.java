package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmProductVariantGroupValueResponseDto;
import com.jippy.foodandmart.dto.FmProductVariantValueRequestDto;

import java.util.List;

public interface IFmProductVariantGroupValueService {

    FmProductVariantGroupValueResponseDto saveVariantGroupValue(
            Integer groupId,
            FmProductVariantValueRequestDto request);

    List<FmProductVariantGroupValueResponseDto> getVariantGroupValues(Integer groupId);

    FmProductVariantGroupValueResponseDto getVariantGroupValueById(
            Integer groupId,
            Integer valueId);

    void deleteVariantGroupValue(
            Integer groupId,
            Integer valueId);
}