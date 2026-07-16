package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmProductVariantGroupRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantGroupResponseDto;
import com.jippy.foodandmart.entity.FmProductVariantGroup;

public final class FmProductVariantGroupMapper {

    private FmProductVariantGroupMapper() {
    }

    public static FmProductVariantGroup toEntity(
            FmProductVariantGroupRequestDto dto) {

        FmProductVariantGroup entity = new FmProductVariantGroup();

        entity.setGroupName(dto.getGroupName().trim());
        entity.setSelectionType(dto.getSelectionType().trim().toUpperCase());
        entity.setMinSelection(dto.getMinSelection());
        entity.setMaxSelection(dto.getMaxSelection());
        entity.setDisplayOrder(dto.getDisplayOrder());
        entity.setIsActive(Boolean.TRUE);

        return entity;
    }

    public static FmProductVariantGroupResponseDto toResponseDto(
            FmProductVariantGroup entity) {

        FmProductVariantGroupResponseDto dto =
                new FmProductVariantGroupResponseDto();

        dto.setProductVariantGroupsId(entity.getProductVariantGroupsId());
        dto.setGroupName(entity.getGroupName());
        dto.setSelectionType(entity.getSelectionType());
        dto.setMinSelection(entity.getMinSelection());
        dto.setMaxSelection(entity.getMaxSelection());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}