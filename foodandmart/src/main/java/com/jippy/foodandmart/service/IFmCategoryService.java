package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;

public interface IFmCategoryService {

    FmCreateCategoryResponseDto createCategory(FmCreateCategoryRequestDto request);

}
