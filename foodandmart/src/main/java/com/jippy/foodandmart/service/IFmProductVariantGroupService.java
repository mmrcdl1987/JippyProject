package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmProductVariantGroupRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantGroupResponseDto;

import java.util.List;

public interface IFmProductVariantGroupService {

    FmProductVariantGroupResponseDto saveVariantGroup(
            FmProductVariantGroupRequestDto request);

    List<FmProductVariantGroupResponseDto> getAllVariantGroups();

    FmProductVariantGroupResponseDto getVariantGroupById(Integer groupId);

    void deleteVariantGroup(Integer groupId);
}