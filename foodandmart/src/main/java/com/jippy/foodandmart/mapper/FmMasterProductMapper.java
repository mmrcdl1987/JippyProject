
        package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmMasterProductRequest;
import com.jippy.foodandmart.dto.FmMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmMasterProduct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public final class FmMasterProductMapper {

    private FmMasterProductMapper() {
    }

    // ============================================================
    // CREATE VALIDATION
    // ============================================================

    public static void validateForCreate(FmMasterProductRequest req) {

        if (req == null)
            throw new IllegalArgumentException("Request cannot be null.");

        if (req.getMasterProductName() == null
                || req.getMasterProductName().isBlank()) {

            throw new IllegalArgumentException(
                    "Master product name cannot be blank."
            );
        }

        if (req.getCategoryId() == null)
            throw new IllegalArgumentException("Category ID is required.");

        if (req.getCategoryName() == null
                || req.getCategoryName().isBlank()) {

            throw new IllegalArgumentException(
                    "Category name is required."
            );
        }

        validateVegNonVeg(
                req.getVeg(),
                req.getNonVeg()
        );
    }

    // ============================================================
    // UPDATE VALIDATION
    // ============================================================

    public static void validateForUpdate(FmMasterProductRequest req) {

        if (req == null)
            throw new IllegalArgumentException("Request cannot be null.");

        if (req.getMasterProductName() != null
                && req.getMasterProductName().isBlank()) {

            throw new IllegalArgumentException(
                    "Master product name cannot be blank."
            );
        }

        if (req.getCategoryName() != null
                && req.getCategoryName().isBlank()) {

            throw new IllegalArgumentException(
                    "Category name cannot be blank."
            );
        }
    }

    // ============================================================
    // VEG / NON-VEG VALIDATION
    // ============================================================

    /**
     * Validate Veg / Non-Veg values.
     *
     * Allowed:
     *
     * Veg = 1, NonVeg = 0
     * Veg = 0, NonVeg = 1
     */
    private static void validateVegNonVeg(
            Integer veg,
            Integer nonVeg) {

        int vegValue = (veg == null) ? 0 : veg;
        int nonVegValue = (nonVeg == null) ? 0 : nonVeg;

        // Only 0 or 1 are allowed
        if ((vegValue != 0 && vegValue != 1)
                || (nonVegValue != 0 && nonVegValue != 1)) {

            throw new IllegalArgumentException(
                    "Veg and Non-Veg values must be either 0 or 1."
            );
        }

        // Exactly one should be selected
        if (vegValue + nonVegValue != 1) {

            throw new IllegalArgumentException(
                    "Please select either Veg or Non-Veg."
            );
        }
    }

    // ============================================================
    // FILTER TYPE VALIDATION
    // ============================================================

    public static String validateType(String type) {

        if (type == null || type.isBlank()) {

            throw new IllegalArgumentException(
                    "Filter type cannot be blank."
            );
        }

        String t = type.trim().toLowerCase();

        if (!t.equals("all")
                && !t.equals("veg")
                && !t.equals("nonveg")) {

            throw new IllegalArgumentException(
                    "Invalid type. Allowed: all, veg, nonveg."
            );
        }

        return t;
    }

    // ============================================================
    // SEARCH VALIDATION
    // ============================================================

    public static String validateSearchKeyword(String keyword) {

        if (keyword == null
                || keyword.isBlank()
                || keyword.trim().length() < 2) {

            throw new IllegalArgumentException(
                    "Search keyword must be at least 2 characters."
            );
        }

        return keyword.trim();
    }

    // ============================================================
    // PHOTO VALIDATION
    // ============================================================

    public static void validatePhoto(
            String contentType,
            long size) {

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "Not a valid image file."
            );
        }

        if (size > 2 * 1024 * 1024L) {

            throw new IllegalArgumentException(
                    "Photo must be under 2 MB."
            );
        }
    }

    // ============================================================
    // DTO -> ENTITY
    // ============================================================

    public static FmMasterProduct toEntity(
            FmMasterProductRequest dto) {

        FmMasterProduct entity = new FmMasterProduct();

        entity.setMasterProductName(
                dto.getMasterProductName().trim()
        );

        entity.setDescription(
                dto.getDescription()
        );

        entity.setShortDescription(
                dto.getShortDescription()
        );

        entity.setPhoto(
                dto.getPhoto()
        );

        entity.setPhotos(
                dto.getPhotos()
        );

        entity.setThumbnail(
                dto.getThumbnail()
        );

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        entity.setCategoryId(
                dto.getCategoryId()
        );

        entity.setCategoryName(
                dto.getCategoryName() != null
                        ? dto.getCategoryName().trim()
                        : null
        );

        entity.setSubCategoryId(
                dto.getSubCategoryId()
        );

        entity.setSubCategoryName(
                dto.getSubCategoryName()
        );

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        entity.setVeg(
                dto.getVeg() != null
                        ? dto.getVeg()
                        : 0
        );

        entity.setNonVeg(
                dto.getNonVeg() != null
                        ? dto.getNonVeg()
                        : 0
        );

        // --------------------------------------------------------
        // FOOD
        // --------------------------------------------------------

        entity.setFoodType(
                dto.getFoodType()
        );

        entity.setCuisineType(
                dto.getCuisineType()
        );

        // --------------------------------------------------------
        // OPTIONS
        // --------------------------------------------------------

        entity.setHasOptions(
                dto.getHasOptions() != null
                        ? dto.getHasOptions()
                        : 0
        );

        entity.setOptionsEnabled(
                dto.getOptionsEnabled() != null
                        ? dto.getOptionsEnabled()
                        : 0
        );

        entity.setOptions(
                dto.getOptions()
        );

        // --------------------------------------------------------
        // NUTRITION
        // --------------------------------------------------------

        entity.setCalories(
                dto.getCalories() != null
                        ? dto.getCalories()
                        : 0
        );

        entity.setProtein(
                dto.getProtein() != null
                        ? dto.getProtein()
                        : 0
        );

        entity.setFats(
                dto.getFats() != null
                        ? dto.getFats()
                        : 0
        );

        entity.setCarbs(
                dto.getCarbs() != null
                        ? dto.getCarbs()
                        : 0
        );

        entity.setGrams(
                dto.getGrams() != null
                        ? dto.getGrams()
                        : 0
        );

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        entity.setProductType(
                dto.getProductType() != null
                        ? dto.getProductType().trim()
                        : null
        );

        // --------------------------------------------------------
        // PUBLISH
        // --------------------------------------------------------

        entity.setPublish(
                dto.getPublish() != null
                        ? dto.getPublish()
                        : 1
        );

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        entity.setCreatedAt(
                LocalDateTime.now()
        );

        entity.setCreatedBy(
                dto.getCreatedBy()
        );

        return entity;
    }

    // ============================================================
    // UPDATE ENTITY
    // ============================================================

    public static void updateEntity(
            FmMasterProduct entity,
            FmMasterProductRequest dto) {

        Integer veg =
                dto.getVeg() != null
                        ? dto.getVeg()
                        : entity.getVeg();

        Integer nonVeg =
                dto.getNonVeg() != null
                        ? dto.getNonVeg()
                        : entity.getNonVeg();

        validateVegNonVeg(
                veg,
                nonVeg
        );

        // --------------------------------------------------------
        // BASIC INFORMATION
        // --------------------------------------------------------

        if (dto.getMasterProductName() != null)
            entity.setMasterProductName(
                    dto.getMasterProductName().trim()
            );

        if (dto.getDescription() != null)
            entity.setDescription(
                    dto.getDescription()
            );

        if (dto.getShortDescription() != null)
            entity.setShortDescription(
                    dto.getShortDescription()
            );

        if (dto.getPhoto() != null)
            entity.setPhoto(
                    dto.getPhoto()
            );

        if (dto.getPhotos() != null)
            entity.setPhotos(
                    dto.getPhotos()
            );

        if (dto.getThumbnail() != null)
            entity.setThumbnail(
                    dto.getThumbnail()
            );

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        if (dto.getCategoryId() != null)
            entity.setCategoryId(
                    dto.getCategoryId()
            );

        if (dto.getCategoryName() != null)
            entity.setCategoryName(
                    dto.getCategoryName().trim()
            );

        if (dto.getSubCategoryId() != null)
            entity.setSubCategoryId(
                    dto.getSubCategoryId()
            );

        if (dto.getSubCategoryName() != null)
            entity.setSubCategoryName(
                    dto.getSubCategoryName()
            );

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        if (dto.getVeg() != null)
            entity.setVeg(
                    dto.getVeg()
            );

        if (dto.getNonVeg() != null)
            entity.setNonVeg(
                    dto.getNonVeg()
            );

        // --------------------------------------------------------
        // FOOD
        // --------------------------------------------------------

        if (dto.getFoodType() != null)
            entity.setFoodType(
                    dto.getFoodType()
            );

        if (dto.getCuisineType() != null)
            entity.setCuisineType(
                    dto.getCuisineType()
            );

        // --------------------------------------------------------
        // OPTIONS
        // --------------------------------------------------------

        if (dto.getHasOptions() != null)
            entity.setHasOptions(
                    dto.getHasOptions()
            );

        if (dto.getOptionsEnabled() != null)
            entity.setOptionsEnabled(
                    dto.getOptionsEnabled()
            );

        if (dto.getOptions() != null)
            entity.setOptions(
                    dto.getOptions()
            );

        // --------------------------------------------------------
        // NUTRITION
        // --------------------------------------------------------

        if (dto.getCalories() != null)
            entity.setCalories(
                    dto.getCalories()
            );

        if (dto.getProtein() != null)
            entity.setProtein(
                    dto.getProtein()
            );

        if (dto.getFats() != null)
            entity.setFats(
                    dto.getFats()
            );

        if (dto.getCarbs() != null)
            entity.setCarbs(
                    dto.getCarbs()
            );

        if (dto.getGrams() != null)
            entity.setGrams(
                    dto.getGrams()
            );

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        if (dto.getProductType() != null)
            entity.setProductType(
                    dto.getProductType().trim()
            );

        // --------------------------------------------------------
        // PUBLISH
        // --------------------------------------------------------

        if (dto.getPublish() != null)
            entity.setPublish(
                    dto.getPublish()
            );

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        entity.setUpdatedAt(
                LocalDateTime.now()
        );

        entity.setUpdatedBy(
                dto.getUpdatedBy()
        );
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public FmMasterProductResponseDto toResponseDto(
            FmMasterProduct product) {

        FmMasterProductResponseDto dto =
                new FmMasterProductResponseDto();

        dto.setMasterProductId(
                product.getMasterProductId()
        );

        dto.setMasterProductName(
                product.getMasterProductName()
        );

        dto.setCategoryId(
                product.getCategoryId()
        );

        dto.setCategoryName(
                product.getCategoryName()
        );

        dto.setPhoto(
                product.getPhoto()
        );

        dto.setThumbnail(
                product.getThumbnail()
        );

        dto.setVeg(
                product.getVeg()
        );

        dto.setNonVeg(
                product.getNonVeg()
        );

        dto.setPublish(
                product.getPublish()
        );

        /*
         * Add this line only after adding productType
         * to FmMasterProductResponseDto.
         */
        dto.setProductType(
                product.getProductType()
        );

        return dto;
    }
}

