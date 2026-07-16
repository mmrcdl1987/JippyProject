package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmProductVariantOptionRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantOptionResponseDto;
import com.jippy.foodandmart.entity.FmProductVariantOption;

public final class FmProductVariantOptionMapper {

    private FmProductVariantOptionMapper() {
    }

    /**
     * Request DTO -> Entity
     */
    public static FmProductVariantOption toEntity(
            Integer productId,
            FmProductVariantOptionRequestDto dto) {

        FmProductVariantOption entity = new FmProductVariantOption();

        entity.setProductId(productId);
        entity.setProductVariantGroupValuesId(dto.getProductVariantGroupValuesId());
        entity.setPriceType(dto.getPriceType().trim().toUpperCase());
        entity.setVariantPrice(dto.getVariantPrice());
        entity.setIsActive(Boolean.TRUE);

        return entity;
    }

    /**
     * Entity -> Response DTO
     */
    public static FmProductVariantOptionResponseDto toResponseDto(
            FmProductVariantOption entity) {

        FmProductVariantOptionResponseDto dto =
                new FmProductVariantOptionResponseDto();

        dto.setProductVariantOptionsId(entity.getProductVariantOptionsId());
        dto.setProductId(entity.getProductId());
        dto.setProductVariantGroupValuesId(entity.getProductVariantGroupValuesId());

        if (entity.getProductVariantGroupValue() != null) {

            dto.setVariantName(
                    entity.getProductVariantGroupValue().getVariantName());

            if (entity.getProductVariantGroupValue().getProductVariantGroup() != null) {

                dto.setGroupName(
                        entity.getProductVariantGroupValue()
                                .getProductVariantGroup()
                                .getGroupName());
            }
        }

        dto.setPriceType(entity.getPriceType());
        dto.setVariantPrice(entity.getVariantPrice());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}