package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmCuisineTypeRequestDTO;
import com.jippy.foodandmart.dto.FmCuisineTypeResponseDTO;
import com.jippy.foodandmart.entity.FmCuisineType;

public final class FmCuisineTypeMapper {

    private FmCuisineTypeMapper() {
    }

    public static FmCuisineType toEntity(
            FmCuisineTypeRequestDTO dto) {

        FmCuisineType entity = new FmCuisineType();

        entity.setCuisineTypesName(
                dto.getCuisineTypesName()
                        .trim()
                        .toUpperCase()
        );

        entity.setCreatedBy(dto.getUserId());

        return entity;
    }

    public static void updateEntity(
            FmCuisineType entity,
            FmCuisineTypeRequestDTO dto) {

        entity.setCuisineTypesName(
                dto.getCuisineTypesName()
                        .trim()
                        .toUpperCase()
        );

        entity.setUpdatedBy(dto.getUserId());
    }

    public static FmCuisineTypeResponseDTO toResponseDTO(
            FmCuisineType entity) {

        FmCuisineTypeResponseDTO dto =
                new FmCuisineTypeResponseDTO();

        dto.setCuisineTypeId(
                entity.getCuisineTypesId()
        );

        dto.setCuisineTypeName(
                entity.getCuisineTypesName()
        );

        return dto;
    }
}