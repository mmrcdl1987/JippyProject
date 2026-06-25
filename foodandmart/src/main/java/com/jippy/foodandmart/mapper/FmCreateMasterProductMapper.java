package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmCreateMasterProductRequestDto;
import com.jippy.foodandmart.dto.FmCreateMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmMasterProduct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FmCreateMasterProductMapper {

    public static void validate(FmCreateMasterProductRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        if (request.getMasterProductName() == null
                || request.getMasterProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Master Product Name is required.");
        }

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category Id is required.");
        }

        if (request.getPhoto() == null
                || request.getPhoto().trim().isEmpty()) {
            throw new IllegalArgumentException("Photo is required.");
        }

        if (request.getIsVeg() == null) {
            throw new IllegalArgumentException("Veg/Non Veg selection is required.");
        }
    }

    public static FmMasterProduct toEntity(
            FmCreateMasterProductRequestDto request,
            String categoryName,
            Integer createdBy) {

        FmMasterProduct entity = new FmMasterProduct();

        entity.setMasterProductName(request.getMasterProductName().trim());

        entity.setDescription(request.getDescription());
        entity.setShortDescription(request.getShortDescription());

        entity.setPhoto(request.getPhoto());
        entity.setPhotos(request.getPhotos());
        entity.setThumbnail(request.getThumbnail());

        entity.setCategoryId(request.getCategoryId());
        entity.setCategoryName(categoryName);

        entity.setVeg(Boolean.TRUE.equals(request.getIsVeg()) ? 1 : 0);
        entity.setNonVeg(Boolean.TRUE.equals(request.getIsVeg()) ? 0 : 1);

        entity.setFoodType(request.getFoodType());
        entity.setCuisineType(request.getCuisineType());

        entity.setHasOptions(0);
        entity.setOptionsEnabled(0);
        entity.setOptions(null);

        entity.setCalories(0);
        entity.setProtein(0);
        entity.setFats(0);
        entity.setCarbs(0);
        entity.setGrams(0);

        entity.setPublish(1);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);

        return entity;
    }

    public FmCreateMasterProductResponseDto toResponseDto(
            FmMasterProduct entity) {

        FmCreateMasterProductResponseDto response =
                new FmCreateMasterProductResponseDto();

        response.setMasterProductId(entity.getMasterProductId());
        response.setMasterProductName(entity.getMasterProductName());
        response.setCategoryId(entity.getCategoryId());
        response.setCategoryName(entity.getCategoryName());
        response.setPhoto(entity.getPhoto());
        response.setThumbnail(entity.getThumbnail());
        response.setVeg(entity.getVeg());
        response.setNonVeg(entity.getNonVeg());
        response.setPublish(entity.getPublish());

        return response;
    }
}