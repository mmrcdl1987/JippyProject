package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmProductVariantGroupValueResponseDto;
import com.jippy.foodandmart.dto.FmProductVariantValueRequestDto;
import com.jippy.foodandmart.entity.FmProductVariantGroupValue;

public final class FmProductVariantGroupValueMapper {

    private FmProductVariantGroupValueMapper() {
    }

    public static FmProductVariantGroupValue toEntity(
            Integer productVariantGroupId,
            FmProductVariantValueRequestDto dto) {

        FmProductVariantGroupValue entity = new FmProductVariantGroupValue();

        entity.setProductVariantGroupsId(productVariantGroupId);
        entity.setVariantName(dto.getVariantName().trim());
        entity.setIsActive(Boolean.TRUE);

        return entity;
    }

    public static FmProductVariantGroupValueResponseDto toResponseDto(
            FmProductVariantGroupValue entity) {

        FmProductVariantGroupValueResponseDto dto =
                new FmProductVariantGroupValueResponseDto();

        dto.setProductVariantGroupValuesId(entity.getProductVariantGroupValuesId());
        dto.setProductVariantGroupsId(entity.getProductVariantGroupsId());
        dto.setVariantName(entity.getVariantName());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}