package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FmProductService {
    /**
     * Maps selected products into outlet products.
     * <p>
     * Creates:
     * - Product
     * - Product Available Timings
     * - Product Variant Groups
     * - Product Variant Group Values
     * - Product Variant Options
     *
     * @param request mapping request
     * @return mapping summary
     */
    FmMapToProductResult mapToProducts(
            FmMapToProduct request);

    /**
     * Maps all published master products of a category
     * into an outlet category.
     * <p>
     * Creates:
     * - Products
     * - Product Available Timings
     * - Product Variant Groups
     * - Product Variant Group Values
     * - Product Variant Options
     *
     * @param outletCategoryId outlet category id
     * @return mapping summary
     */
//    FmMasterProductMappingResultDTO mapFromMasterByCategory(
//            Integer outletCategoryId);

    /**
     * Fetch complete product details for edit screen.
     * <p>
     * Includes:
     * - Product Details
     * - Product Timings
     * - Variant Groups
     * - Variant Options
     *
     * @param productId product id
     * @return product details
     */
    FmProductUpdateResponseDto getProductById(
            Integer productId);

    /**
     * Update merchant product.
     * <p>
     * Updates:
     * - Product Details
     * - Product Timings
     * - Variant Options
     *
     * @param productId product id
     * @param request   update request
     * @return updated product
     */
    FmProductUpdateResponseDto updateProduct(
            Integer productId,
            FmProductUpdateRequestDto request);


    boolean existsProductInOutlet(
            Integer outletId,
            Integer productId);

    List<Integer> getActiveProductIdsByOutlet(
            Integer outletId);

    @Transactional(readOnly = true)
    List<FmProductPriceResponse> getProductsByOutlet(Integer outletId);
}
