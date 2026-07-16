package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmProductVariantOptionRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantOptionResponseDto;

import java.util.List;

public interface IFmProductVariantOptionService {

    /**
     * Create / Update Product Variant Option
     */
    FmProductVariantOptionResponseDto saveProductVariantOption(
            Integer productId,
            FmProductVariantOptionRequestDto request);

    /**
     * Get all Variant Options of a Product
     */
    List<FmProductVariantOptionResponseDto> getProductVariantOptions(
            Integer productId);

    /**
     * Get Product Variant Option by Id
     */
    FmProductVariantOptionResponseDto getProductVariantOptionById(
            Integer productId,
            Integer productVariantOptionId);

    /**
     * Soft Delete Product Variant Option
     */
    void deleteProductVariantOption(
            Integer productId,
            Integer productVariantOptionId);
}