package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductVariant;
import com.jippy.foodandmart.projections.FmMasterProductCategoryProjection;
import com.jippy.foodandmart.projections.FmProductCategoryProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FmProductMapper {

    // ============================================================
    // CATEGORY UPDATE RESPONSE MAPPER
    // ============================================================

    public static FmProductCategoryUpdateResponseDto mapCategoryUpdateResponse(String productType, String productName, Integer updatedCategoryId, Integer updatedRecords) {

        FmProductCategoryUpdateResponseDto dto = new FmProductCategoryUpdateResponseDto();

        dto.setProductType(productType);
        dto.setProductName(productName);
        dto.setUpdatedCategoryId(updatedCategoryId);
        dto.setUpdatedRecords(updatedRecords);
        dto.setMessage("Category updated successfully.");

        return dto;
    }

    // ============================================================
    // PRODUCT CATEGORY PROJECTION -> RESPONSE DTO
    // ============================================================

    public static FmProductCategoryResponseDto mapProductCategoryProjectionToDto(FmProductCategoryProjection projection) {

        FmProductCategoryResponseDto dto = new FmProductCategoryResponseDto();

        dto.setProductId(projection.getProductId());
        dto.setProductName(projection.getProductName());
        dto.setOutletCategoryId(projection.getOutletCategoryId());
        dto.setOutletId(projection.getOutletId());
        dto.setCategoryId(projection.getCategoryId());
        dto.setCategoryName(projection.getCategoryName());
        dto.setOutletName(projection.getOutletName());

        return dto;
    }

    // ============================================================
    // MASTER PRODUCT CATEGORY PROJECTION -> RESPONSE DTO
    // ============================================================

    public static FmMasterProductCategoryResponseDto mapMasterProductCategoryProjectionToDto(FmMasterProductCategoryProjection projection) {

        FmMasterProductCategoryResponseDto dto = new FmMasterProductCategoryResponseDto();

        dto.setMasterProductId(projection.getMasterProductId());

        dto.setMasterProductName(projection.getMasterProductName());

        dto.setCategoryId(projection.getCategoryId());

        dto.setCategoryName(projection.getCategoryName());

        return dto;
    }

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    private FmProductMapper() {
    }

    // ============================================================
    // BULK UPLOAD MAPPERS
    // ============================================================

    /**
     * Maps product name -> merchant price.
     */
    public static Map<String, Double> priceMapper = new HashMap<String, Double>();

    /**
     * Maps product name -> day-of-week name.
     * <p>
     * Example:
     * Chicken Biryani -> Monday
     */
    public static Map<String, String> dayOfWeekMapper = new HashMap<String, String>();

    /**
     * Maps product name -> raw CSV timing string.
     * <p>
     * Example:
     * Chicken Biryani -> 9:00-22:00
     */
    public static Map<String, String> timingMapper = new HashMap<String, String>();

    // ============================================================
    // PRODUCT DTO -> ENTITY
    // ============================================================

    /**
     * Converts FmProductDto into FmProduct entity.
     * <p>
     * Product type is now also persisted into:
     * <p>
     * jippy_fm.products.product_type
     */
    public static FmProduct toEntity(FmProductDto dto, Integer outletCategoryId, Integer createdBy) {

        FmProduct entity = new FmProduct();

        // --------------------------------------------------------
        // BASIC PRODUCT INFORMATION
        // --------------------------------------------------------

        entity.setProductName(dto.getProductName() != null ? dto.getProductName().trim() : null);

        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");

        entity.setMerchantPrice(dto.getMerchantPrice());

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        entity.setIsVeg(dto.getIsVeg() != null ? dto.getIsVeg() : true);

        // --------------------------------------------------------
        // VARIANTS
        // --------------------------------------------------------

        entity.setHasProductVariants(dto.getHasProductVariants() != null ? dto.getHasProductVariants() : false);

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        entity.setProductType(dto.getProductType() != null ? dto.getProductType().trim() : null);

        // --------------------------------------------------------
        // OUTLET CATEGORY
        // --------------------------------------------------------

        entity.setOutletCategoryId(outletCategoryId);

        // --------------------------------------------------------
        // CREATED INFORMATION
        // --------------------------------------------------------

        entity.setCreatedAt(LocalDateTime.now());

        entity.setCreatedBy(createdBy);

        return entity;
    }

    // ============================================================
    // PRODUCT VARIANT DTO -> ENTITY
    // ============================================================

    /**
     * Converts FmProductVariantDTO into FmProductVariant entity.
     */
    public static FmProductVariant toVariantEntity(FmProductVariantDTO dto, Integer productId, Integer createdBy) {

        FmProductVariant entity = new FmProductVariant();

        entity.setProductId(productId);

        entity.setVariantName(dto.getVariantName() != null ? dto.getVariantName().trim() : null);

        entity.setMerchantPrice(dto.getMerchantPrice());

        entity.setCreatedAt(LocalDateTime.now());

        entity.setCreatedBy(createdBy);

        return entity;
    }

    // ============================================================
    // ENTITY -> PRODUCT DTO
    // ============================================================

    /**
     * Converts FmProduct entity into FmProductDto.
     * <p>
     * Product type is returned to the frontend/mobile application.
     */
    public static FmProductDto toDTO(FmProduct product) {

        FmProductDto dto = new FmProductDto();

        // --------------------------------------------------------
        // BASIC PRODUCT INFORMATION
        // --------------------------------------------------------

        dto.setProductId(product.getProductId());

        dto.setProductName(product.getProductName());

        dto.setDescription(product.getDescription());

        dto.setMerchantPrice(product.getMerchantPrice());

        // --------------------------------------------------------
        // VEG / NON-VEG
        // --------------------------------------------------------

        dto.setIsVeg(product.getIsVeg());

        // --------------------------------------------------------
        // VARIANTS
        // --------------------------------------------------------

        dto.setHasProductVariants(product.getHasProductVariants());

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        dto.setProductType(product.getProductType());

        // --------------------------------------------------------
        // VARIANT LIST
        // --------------------------------------------------------

        List<FmProductVariantDTO> variants = product.getVariants() != null ? product.getVariants().stream().map(FmProductMapper::toVariantDTO).collect(Collectors.toList()) : Collections.emptyList();

        dto.setVariants(variants);

        return dto;
    }

    // ============================================================
    // VARIANT ENTITY -> DTO
    // ============================================================

    /**
     * Converts FmProductVariant entity into DTO.
     */
    public static FmProductVariantDTO toVariantDTO(FmProductVariant variant) {

        FmProductVariantDTO dto = new FmProductVariantDTO();

        dto.setVariantId(variant.getProductVariantId());

        dto.setVariantName(variant.getVariantName());

        dto.setMerchantPrice(variant.getMerchantPrice());

        return dto;
    }

    // ============================================================
    // PRODUCT DETAIL RESPONSE
    // ============================================================

    public FmProductDetailResponseDto toDto(FmProduct product) {

        if (product == null) {
            return null;
        }

        FmProductDetailResponseDto dto = new FmProductDetailResponseDto();

        // --------------------------------------------------------
        // PRODUCT ID
        // --------------------------------------------------------

        dto.setProductId(product.getProductId());

        // --------------------------------------------------------
        // OUTLET CATEGORY
        // --------------------------------------------------------

        dto.setOutletCategoryId(product.getOutletCategoryId());

        // --------------------------------------------------------
        // PRODUCT DETAILS
        // --------------------------------------------------------

        dto.setProductName(product.getProductName());

        dto.setDescription(product.getDescription());

        dto.setMerchantPrice(product.getMerchantPrice());

        dto.setIsVeg(product.getIsVeg());

        dto.setHasProductVariants(product.getHasProductVariants());

        // --------------------------------------------------------
        // PRODUCT TYPE
        // --------------------------------------------------------

        dto.setProductType(product.getProductType());

        // --------------------------------------------------------
        // IMAGE
        // --------------------------------------------------------

        dto.setImageLink(product.getImageLink());

        // --------------------------------------------------------
        // PHOTOS / THUMBNAIL
        // --------------------------------------------------------

        // dto.setPhotos(product.getPhotos());
        // dto.setThumbnail(product.getThumbnail());

        // --------------------------------------------------------
        // PRODUCT ACTIVE STATUS
        // --------------------------------------------------------

        dto.setAvailable("Y".equalsIgnoreCase(product.getIsActive()));

        return dto;
    }
}