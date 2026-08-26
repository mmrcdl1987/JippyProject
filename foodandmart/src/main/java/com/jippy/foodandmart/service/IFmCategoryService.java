package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.dto.FmUpdateCategoryRequestDto;

import java.util.List;

public interface IFmCategoryService {

    FmCreateCategoryResponseDto createCategory(FmCreateCategoryRequestDto request);


    List<FmCreateCategoryResponseDto> getHomeOrAllCategories(String filter);

    FmCreateCategoryResponseDto updateCategory(
            FmUpdateCategoryRequestDto request
    );
}
