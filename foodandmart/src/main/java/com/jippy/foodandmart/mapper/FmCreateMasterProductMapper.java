package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmCreateMasterProductRequestDto;
import com.jippy.foodandmart.dto.FmCreateMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmMasterProduct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FmCreateMasterProductMapper {

    // ============================================================
    // VALIDATION
    // ============================================================

    public static void validate(
            FmCreateMasterProductRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null."
            );
        }

        if (request.getMasterProductName() == null
                || request.getMasterProductName()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Master Product Name is required."
            );
        }

        if (request.getCategoryId() == null) {

            throw new IllegalArgumentException(
                    "Category Id is required."
            );
        }

        /*
         * PHOTO IS OPTIONAL.
         *
         * Do NOT validate photo here.
         *
         * Product can be created/added to outlet
         * without a photo.
         */

        if (request.getIsVeg() == null) {

            throw new IllegalArgumentException(
                    "Veg/Non Veg selection is required."
            );
        }

        // --------------------------------------------------------
        // PRODUCT TYPE VALIDATION
        // --------------------------------------------------------

        if (request.getProductType() != null
                && request.getProductType().trim().length() > 20) {

            throw new IllegalArgumentException(
                    "Product Type cannot exceed 20 characters."
            );
        }
    }

    // ============================================================
    // REQUEST DTO -> ENTITY
    // ============================================================

    public static FmMasterProduct toEntity(
            FmCreateMasterProductRequestDto request,
            String categoryName,
            Integer createdBy) {

        FmMasterProduct entity =
                new FmMasterProduct();

        // --------------------------------------------------------
        // BASIC INFORMATION
        // --------------------------------------------------------

        entity.setMasterProductName(
                request.getMasterProductName()
                        .trim()
        );

        entity.setDescription(
                request.getDescription()
        );

        entity.setShortDescription(
                request.getShortDescription()
        );

        // --------------------------------------------------------
        // PHOTO
        // --------------------------------------------------------

        /*
         * PHOTO IS OPTIONAL.
         *
         * If photo is not provided, null will be stored.
         */
        entity.setPhoto(
                request.getPhoto()
        );

        entity.setPhotos(
                request.getPhotos()
        );

        entity.setThumbnail(
                request.getThumbnail()
        );

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        entity.setCategoryId(
                request.getCategoryId()
        );

        entity.setCategoryName(
                categoryName
        );

        // --------------------------------------------------------
        // VEG / NON VEG
        // --------------------------------------------------------

        entity.setVeg(
                Boolean.TRUE.equals(
                        request.getIsVeg()
                ) ? 1 : 0
        );

        entity.setNonVeg(
                Boolean.TRUE.equals(
                        request.getIsVeg()
                ) ? 0 : 1
        );

        // --------------------------------------------------------
        // FOOD
        // --------------------------------------------------------

        entity.setFoodType(
                request.getFoodType()
        );

        entity.setCuisineType(
                request.getCuisineType()
        );

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        entity.setProductType(
                request.getProductType() != null
                        ? request.getProductType()
                        .trim()
                        : null
        );

        // --------------------------------------------------------
        // OPTIONS
        // --------------------------------------------------------

        entity.setHasOptions(0);

        entity.setOptionsEnabled(0);

        entity.setOptions(null);

        // --------------------------------------------------------
        // NUTRITION
        // --------------------------------------------------------

        entity.setCalories(0);

        entity.setProtein(0);

        entity.setFats(0);

        entity.setCarbs(0);

        entity.setGrams(0);

        // --------------------------------------------------------
        // PUBLISH
        // --------------------------------------------------------

        entity.setPublish(1);

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        entity.setCreatedAt(
                LocalDateTime.now()
        );

        entity.setCreatedBy(
                createdBy
        );

        return entity;
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public FmCreateMasterProductResponseDto toResponseDto(
            FmMasterProduct entity) {

        FmCreateMasterProductResponseDto response =
                new FmCreateMasterProductResponseDto();

        response.setMasterProductId(
                entity.getMasterProductId()
        );

        response.setMasterProductName(
                entity.getMasterProductName()
        );

        response.setCategoryId(
                entity.getCategoryId()
        );

        response.setCategoryName(
                entity.getCategoryName()
        );

        response.setPhoto(
                entity.getPhoto()
        );

        response.setThumbnail(
                entity.getThumbnail()
        );

        response.setVeg(
                entity.getVeg()
        );

        response.setNonVeg(
                entity.getNonVeg()
        );

        /*
         * Product type returned in API response.
         *
         * This requires productType to exist in
         * FmCreateMasterProductResponseDto.
         */
        response.setProductType(
                entity.getProductType()
        );

        response.setPublish(
                entity.getPublish()
        );

        return response;
    }
}