package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.entity.FmCategory;

import java.time.LocalDateTime;

public class CategoryMapper {

    private CategoryMapper() {
    }

    public static FmCategory toEntity(FmCreateCategoryRequestDto request, Integer createdBy) {

        FmCategory entity = new FmCategory();
        entity.setCategoryName(request.getCategoryName().trim());
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    public static FmCreateCategoryResponseDto toResponseDto(FmCategory entity) {

        FmCreateCategoryResponseDto response = new FmCreateCategoryResponseDto();
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryName(entity.getCategoryName());

        return response;
    }
}