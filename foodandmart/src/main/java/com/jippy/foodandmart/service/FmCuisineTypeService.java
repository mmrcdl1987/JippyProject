package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCuisineTypeRequestDTO;
import com.jippy.foodandmart.dto.FmCuisineTypeResponseDTO;

import java.util.List;

public interface FmCuisineTypeService {

    FmCuisineTypeResponseDTO createCuisineType(
            FmCuisineTypeRequestDTO dto
    );

    FmCuisineTypeResponseDTO getCuisineTypeById(
            Integer cuisineTypesId
    );

    List<FmCuisineTypeResponseDTO> getAllCuisineTypes();

    FmCuisineTypeResponseDTO updateCuisineType(
            Integer cuisineTypesId,
            FmCuisineTypeRequestDTO dto
    );
}