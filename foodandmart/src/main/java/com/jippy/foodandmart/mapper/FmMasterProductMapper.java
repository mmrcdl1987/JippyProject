package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmMasterProductRequest;
import com.jippy.foodandmart.dto.FmMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmMasterProduct;
import org.springframework.stereotype.Component;

@Component
public final class FmMasterProductMapper {

    private FmMasterProductMapper() {
    }

    // ============================================================
    // CREATE VALIDATION
    // ============================================================

    public static void validateForCreate(FmMasterProductRequest req) {

        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        // --------------------------------------------------------
        // MASTER PRODUCT NAME
        // --------------------------------------------------------

        if (req.getMasterProductName() == null
                || req.getMasterProductName().isBlank()) {

            throw new IllegalArgumentException(
                    "Master product name cannot be blank."
            );
        }

        if (req.getMasterProductName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Master product name cannot exceed 100 characters."
            );
        }

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        if (req.getCategoryId() == null) {
            throw new IllegalArgumentException(
                    "Category ID is required."
            );
        }

        if (req.getCategoryName() == null
                || req.getCategoryName().isBlank()) {

            throw new IllegalArgumentException(
                    "Category name is required."
            );
        }

        if (req.getCategoryName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 100 characters."
            );
        }

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        if (req.getIsVeg() == null) {
            throw new IllegalArgumentException(
                    "Veg/Non-Veg selection is required."
            );
        }

        // --------------------------------------------------------
        // HAS OPTIONS
        // --------------------------------------------------------

        if (req.getHasOptions() == null) {
            throw new IllegalArgumentException(
                    "Has Options value is required."
            );
        }

        validateBinaryValue(
                req.getHasOptions(),
                "Has Options"
        );

        // --------------------------------------------------------
        // OPTIONS
        // --------------------------------------------------------

        if (req.getHasOptions() == 0
                && req.getOptions() != null
                && !req.getOptions().isBlank()) {

            throw new IllegalArgumentException(
                    "Options must be empty when hasOptions is 0."
            );
        }

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        if (req.getProductType() == null
                || req.getProductType().isBlank()) {

            throw new IllegalArgumentException(
                    "Product type is required."
            );
        }

        if (req.getProductType().trim().length() > 10) {
            throw new IllegalArgumentException(
                    "Product type cannot exceed 10 characters."
            );
        }
    }

    // ============================================================
    // UPDATE VALIDATION
    // ============================================================

    public static void validateForUpdate(FmMasterProductRequest req) {

        if (req == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null."
            );
        }

        // --------------------------------------------------------
        // MASTER PRODUCT NAME
        // --------------------------------------------------------

        if (req.getMasterProductName() != null) {

            if (req.getMasterProductName().isBlank()) {
                throw new IllegalArgumentException(
                        "Master product name cannot be blank."
                );
            }

            if (req.getMasterProductName().trim().length() > 100) {
                throw new IllegalArgumentException(
                        "Master product name cannot exceed 100 characters."
                );
            }
        }

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        if (req.getCategoryName() != null) {

            if (req.getCategoryName().isBlank()) {
                throw new IllegalArgumentException(
                        "Category name cannot be blank."
                );
            }

            if (req.getCategoryName().trim().length() > 100) {
                throw new IllegalArgumentException(
                        "Category name cannot exceed 100 characters."
                );
            }
        }

        if (req.getHasOptions() != null) {
            validateBinaryValue(
                    req.getHasOptions(),
                    "Has Options"
            );
        }

        if (req.getProductType() != null
                && req.getProductType().trim().length() > 10) {

            throw new IllegalArgumentException(
                    "Product type cannot exceed 10 characters."
            );
        }

        if (req.getIsVeg() == null) {
            // Allowed during partial update.
            // Existing database value will be retained.
        }
    }

    // ============================================================
    // BINARY VALUE VALIDATION
    // ============================================================

    private static void validateBinaryValue(
            Integer value,
            String fieldName) {

        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        if (value != 0 && value != 1) {
            throw new IllegalArgumentException(
                    fieldName + " must be either 0 or 1."
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

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null."
            );
        }

        FmMasterProduct entity = new FmMasterProduct();

        // --------------------------------------------------------
        // BASIC INFORMATION
        // --------------------------------------------------------

        entity.setMasterProductName(
                dto.getMasterProductName() != null
                        ? dto.getMasterProductName().trim()
                        : null
        );

        entity.setDescription(
                dto.getDescription()
        );

        entity.setPhoto(
                dto.getPhoto()
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

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        entity.setIsVeg(
                dto.getIsVeg()
        );

        // --------------------------------------------------------
        // CUISINE
        // --------------------------------------------------------

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

        /*
         * options is stored as JSONB.
         *
         * Empty options should be stored as null.
         */
        if (dto.getOptions() != null
                && !dto.getOptions().isBlank()) {

            entity.setOptions(
                    dto.getOptions().trim()
            );
        } else {
            entity.setOptions(null);
        }

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        entity.setProductType(
                dto.getProductType() != null
                        ? dto.getProductType().trim()
                        : null
        );

        // --------------------------------------------------------
        // ACTIVE STATUS
        // --------------------------------------------------------

        /*
         * System-generated field.
         *
         * Y = Active
         */
        entity.setIsActive("Y");

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        /*
         * createdAt is handled by @CreationTimestamp.
         */
        entity.setCreatedBy(
                dto.getCreatedBy()
        );

        /*
         * updatedAt / updatedBy remain null
         * for a newly created record.
         */
        entity.setUpdatedBy(null);

        return entity;
    }

    // ============================================================
    // UPDATE ENTITY
    // ============================================================

    public static void updateEntity(
            FmMasterProduct entity,
            FmMasterProductRequest dto) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Existing product cannot be null."
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null."
            );
        }

        // --------------------------------------------------------
        // BASIC INFORMATION
        // --------------------------------------------------------

        if (dto.getMasterProductName() != null) {

            entity.setMasterProductName(
                    dto.getMasterProductName().trim()
            );
        }

        if (dto.getDescription() != null) {

            entity.setDescription(
                    dto.getDescription()
            );
        }

        if (dto.getPhoto() != null) {

            entity.setPhoto(
                    dto.getPhoto()
            );
        }

        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        if (dto.getCategoryId() != null) {

            entity.setCategoryId(
                    dto.getCategoryId()
            );
        }

        if (dto.getCategoryName() != null) {

            entity.setCategoryName(
                    dto.getCategoryName().trim()
            );
        }

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        if (dto.getIsVeg() != null) {

            entity.setIsVeg(
                    dto.getIsVeg()
            );
        }

        // --------------------------------------------------------
        // CUISINE
        // --------------------------------------------------------

        if (dto.getCuisineType() != null) {

            entity.setCuisineType(
                    dto.getCuisineType()
            );
        }

        // --------------------------------------------------------
        // OPTIONS
        // --------------------------------------------------------

        if (dto.getHasOptions() != null) {

            entity.setHasOptions(
                    dto.getHasOptions()
            );
        }

        if (dto.getOptions() != null) {

            if (dto.getOptions().isBlank()) {
                entity.setOptions(null);
            } else {
                entity.setOptions(
                        dto.getOptions().trim()
                );
            }
        }

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        if (dto.getProductType() != null) {

            entity.setProductType(
                    dto.getProductType().trim()
            );
        }

        // --------------------------------------------------------
        // AUDIT
        // --------------------------------------------------------

        /*
         * updatedAt is handled by @UpdateTimestamp.
         */
        entity.setUpdatedBy(
                dto.getUpdatedBy()
        );
    }

    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public FmMasterProductResponseDto toResponseDto(
            FmMasterProduct product) {

        if (product == null) {
            return null;
        }

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

        dto.setIsVeg(
                product.getIsVeg()
        );

        dto.setCuisineType(
                product.getCuisineType()
        );

        dto.setHasOptions(
                product.getHasOptions()
        );

        dto.setOptions(
                product.getOptions()
        );

        dto.setProductType(
                product.getProductType()
        );

        return dto;
    }
}