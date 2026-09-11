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

        // --------------------------------------------------------
        // MASTER PRODUCT NAME
        // --------------------------------------------------------

        if (request.getMasterProductName() == null
                || request.getMasterProductName()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Master Product Name is required."
            );
        }

        if (request.getMasterProductName()
                .trim()
                .length() > 100) {

            throw new IllegalArgumentException(
                    "Master Product Name cannot exceed 100 characters."
            );
        }


        // --------------------------------------------------------
        // CATEGORY ID
        // --------------------------------------------------------

        if (request.getCategoryId() == null) {

            throw new IllegalArgumentException(
                    "Category Id is required."
            );
        }

        if (request.getCategoryId() <= 0) {

            throw new IllegalArgumentException(
                    "Category Id must be greater than 0."
            );
        }


        // --------------------------------------------------------
        // IS VEG
        // --------------------------------------------------------

        /*
         * New database column:
         *
         * is_veg BOOLEAN NOT NULL
         *
         * true  = Veg
         * false = Non-Veg
         */
        if (request.getIsVeg() == null) {

            throw new IllegalArgumentException(
                    "is_veg value is required. Use true or false."
            );
        }


        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        if (request.getProductType() == null
                || request.getProductType()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "Product Type is required."
            );
        }

        if (request.getProductType()
                .trim()
                .length() > 10) {

            throw new IllegalArgumentException(
                    "Product Type cannot exceed 10 characters."
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

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null."
            );
        }


        FmMasterProduct entity =
                new FmMasterProduct();


        // ========================================================
        // BASIC INFORMATION
        // ========================================================

        entity.setMasterProductName(
                request.getMasterProductName()
                        .trim()
        );

        entity.setDescription(
                request.getDescription()
        );


        // ========================================================
        // PHOTO
        // ========================================================

        /*
         * PHOTO IS OPTIONAL.
         *
         * If photo is not provided,
         * null will be stored.
         */
        entity.setPhoto(
                request.getPhoto()
        );


        // ========================================================
        // CATEGORY
        // ========================================================

        entity.setCategoryId(
                request.getCategoryId()
        );

        entity.setCategoryName(
                categoryName
        );


        // ========================================================
        // IS VEG
        // ========================================================

        /*
         * New database structure:
         *
         * is_veg BOOLEAN
         *
         * true  -> Veg
         * false -> Non-Veg
         *
         * Do NOT create veg/nonVeg integer values.
         */
        entity.setIsVeg(
                request.getIsVeg()
        );


        // ========================================================
        // CUISINE
        // ========================================================

        entity.setCuisineType(
                request.getCuisineType()
        );


        // ========================================================
        // PRODUCT TYPE
        // ========================================================

        entity.setProductType(
                request.getProductType() != null
                        ? request.getProductType()
                        .trim()
                        : null
        );


        // ========================================================
        // OPTIONS
        // ========================================================

        /*
         * The create DTO currently does not contain
         * hasOptions/options fields.
         *
         * Therefore the create API uses:
         *
         * has_options = 0
         * options = null
         *
         * This is a system default for the create API.
         *
         * Bulk Excel upload uses the actual Excel
         * has_options and options values.
         */
        entity.setHasOptions(0);

        entity.setOptions(null);


        // ========================================================
        // AUDIT
        // ========================================================

        entity.setCreatedAt(
                LocalDateTime.now()
        );

        entity.setCreatedBy(
                createdBy
        );

        entity.setUpdatedAt(
                null
        );

        entity.setUpdatedBy(
                null
        );


        // ========================================================
        // ACTIVE STATUS
        // ========================================================

        /*
         * Database:
         *
         * is_active VARCHAR(1) NOT NULL
         *
         * Y = Active
         *
         * This is a system status, not product-specific data.
         */
        entity.setIsActive("Y");


        return entity;
    }


    // ============================================================
    // ENTITY -> RESPONSE DTO
    // ============================================================

    public FmCreateMasterProductResponseDto toResponseDto(
            FmMasterProduct entity) {

        if (entity == null) {
            return null;
        }


        FmCreateMasterProductResponseDto response =
                new FmCreateMasterProductResponseDto();


        // --------------------------------------------------------
        // ID
        // --------------------------------------------------------

        response.setMasterProductId(
                entity.getMasterProductId()
        );


        // --------------------------------------------------------
        // NAME
        // --------------------------------------------------------

        response.setMasterProductName(
                entity.getMasterProductName()
        );


        // --------------------------------------------------------
        // CATEGORY
        // --------------------------------------------------------

        response.setCategoryId(
                entity.getCategoryId()
        );

        response.setCategoryName(
                entity.getCategoryName()
        );


        // --------------------------------------------------------
        // PHOTO
        // --------------------------------------------------------

        response.setPhoto(
                entity.getPhoto()
        );


        // --------------------------------------------------------
        // IS VEG
        // --------------------------------------------------------

        /*
         * New API response should use:
         *
         * isVeg = true
         * isVeg = false
         *
         * instead of:
         *
         * veg = 1
         * nonVeg = 0
         */
        response.setIsVeg(
                entity.getIsVeg()
        );


        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        response.setProductType(
                entity.getProductType()
        );


        return response;
    }
}