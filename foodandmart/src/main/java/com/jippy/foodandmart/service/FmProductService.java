package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FmProductService {

    // ============================================================
    // MAP PRODUCTS
    // ============================================================

    /**
     * Maps selected products into outlet products.
     *
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
            FmMapToProduct request
    );


    /**
     * Merchant edit — updates basic fields + merchant price,
     * and edits/adds timings & variant options. Never deletes.
     */
    FmProductUpdateResponseDto merchantEditProduct(Integer productId, FmProductUpdateRequestDto request);

    /**
     * Soft deletes a variant option belonging to a product.
     */
    void deleteProductVariantOption(Integer productId, Integer optionId);

    /**
     * Removes all variant options for a group from a product.
     * The shared group and group values remain active for other products.
     */
    void deleteProductVariantGroup(Integer productId, Integer groupId);

    // ============================================================
    // BULK UPLOAD VARIANTS
    // ============================================================

    /**
     * Uploads product variants in bulk using an Excel file.
     *
     * @param outletId outlet id
     * @param file Excel file containing variant information
     * @return bulk upload result
     */
    FmVariantBulkUploadResponseDto bulkUploadVariants(
            Integer outletId,
            MultipartFile file
    );

    // ============================================================
    // MAP MASTER PRODUCTS
    // ============================================================

    /**
     * Maps all published master products of a category
     * into an outlet category.
     *
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
    // FmMasterProductMappingResultDTO mapFromMasterByCategory(
    //         Integer outletCategoryId
    // );

    // ============================================================
    // GET PRODUCT BY ID
    // ============================================================

    /**
     * Fetch complete product details for edit screen.
     *
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
            Integer productId
    );

    // ============================================================
    // GET COMPLETE PRODUCT DETAILS
    // ============================================================

    /**
     * Fetch complete product details including
     * variant groups and variant options.
     *
     * @param productId product id
     * @return complete product details
     */
    FmProductDetailResponse getProductDetailById(
            Integer productId
    );

    // ============================================================
    // UPDATE PRODUCT
    // ============================================================

    /**
     * Update merchant product.
     *
     * Updates:
     * - Product Details
     * - Product Timings
     * - Variant Options
     *
     * @param productId product id
     * @param request update request
     * @return updated product
     */
    FmProductUpdateResponseDto updateProduct(
            Integer productId,
            FmProductUpdateRequestDto request
    );

    // ============================================================
    // PRODUCT / OUTLET VALIDATION
    // ============================================================

    /**
     * Checks whether a product exists in an outlet.
     *
     * @param outletId outlet id
     * @param productId product id
     * @return true when product belongs to outlet
     */
    boolean existsProductInOutlet(
            Integer outletId,
            Integer productId
    );

    // ============================================================
    // ACTIVE PRODUCTS
    // ============================================================

    /**
     * Gets all active product IDs for an outlet.
     *
     * @param outletId outlet id
     * @return active product IDs
     */
    List<Integer> getActiveProductIdsByOutlet(
            Integer outletId
    );

    // ============================================================
    // PRODUCTS BY OUTLET
    // ============================================================

    /**
     * Gets products and pricing for an outlet.
     *
     * @param outletId outlet id
     * @return product pricing response
     */
    @Transactional(readOnly = true)
    List<FmProductPriceResponse> getProductsByOutlet(
            Integer outletId
    );

    /**
     * Gets all products for an outlet by outlet ID.
     *
     * @param outletId outlet id
     * @return outlet product response list
     */
    List<FmOutletProductResponseDto> getProductsByOutletId(
            Integer outletId
    );

    // ============================================================
    // PRODUCT PRICING BY OUTLET
    // ============================================================

    /**
     * Gets outlet product pricing details.
     *
     * @param outletId outlet id
     * @return outlet product pricing
     */
    List<OutletProductPricingDto> getProductPricingByOutletId(
            Integer outletId
    );

    // ============================================================
    // PRODUCT CATEGORY BY PRODUCT TYPE
    // ============================================================

    /**
     * Gets category information for a product
     * based on product type.
     *
     * Supported product types:
     * - PRODUCT
     * - MASTERPRODUCT
     *
     * @param productName product name
     * @param productType product type
     * @return category information
     */
    Object getCategoryForProductByProductType(
            String productName,
            String productType
    );

    // ============================================================
    // UPDATE PRODUCT CATEGORY BY PRODUCT TYPE
    // ============================================================

    /**
     * Updates category information for a product
     * based on product type.
     *
     * @param request category update request
     * @return category update response
     */
    FmProductCategoryUpdateResponseDto updateCategoryForProductByProductType(
            FmProductCategoryUpdateRequestDto request
    );

    // ============================================================
    // MERCHANT PRICE UPDATE
    // ============================================================

    /**
     * Updates merchant price for a product.
     *
     * Merchant price changes are handled by the product service.
     *
     * @param productId product id
     * @param request merchant price update request
     * @return price update response
     */
    FmMerchantPriceUpdateResponse updateMerchantPrice(
            Integer productId,
            FmMerchantPriceUpdateRequest request
    );

    List<FmOrderItemsEvent> getOrderProductItemsForMerchant(List<Integer> productIds, List<Integer> productVariantIds);
}