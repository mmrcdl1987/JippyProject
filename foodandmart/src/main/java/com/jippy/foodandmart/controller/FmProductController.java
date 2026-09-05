package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.FmProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fm/products")
@RequiredArgsConstructor
@Validated
public class FmProductController {

    private final FmProductService productMappingService;


    /**
     * POST /api/fm/products/from-master
     * <p>
     * Maps selected master products into outlet products.
     */
    @PostMapping("/from-master")
    public ResponseEntity<FmMapToProductResult> mapFromMaster(@RequestBody FmMapToProduct req) {

        log.info("[PRODUCT-MAP] POST /from-master - OutletId={}, CategoryId={}, Products={}", req.getOutletId(), req.getCategoryId(), req.getProducts() == null ? 0 : req.getProducts().size());

        return ResponseEntity.ok(productMappingService.mapToProducts(req));
    }

    /**
     * PUT /api/fm/products/merchant-edit/{productId}
     *
     * Merchant edit — basic fields + merchant price, and edit/add
     * timings & variants (incl. new groups). Never deletes existing records.
     */
    @PutMapping("/updateproduct/{productId}")
    public ResponseEntity<FmProductUpdateResponseDto> merchantEditProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody FmProductUpdateRequestDto request) {

        log.info("[PRODUCT] MERCHANT_EDIT Product. ProductId={}", productId);

        return ResponseEntity.ok(productMappingService.merchantEditProduct(productId, request));
    }

    /**
     * DELETE /api/fm/products/updateproduct/{productId}/variant-options/{optionId}
     *
     * Removes one variant option from this product without deleting shared
     * variant group/value catalogue records.
     */
    @DeleteMapping("/updateproduct/{productId}/variant-options/{optionId}")
    public ResponseEntity<FmApiResponse<Void>> deleteProductVariantOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        log.info("[PRODUCT] DELETE variant option. ProductId={}, OptionId={}",
                productId, optionId);

        productMappingService.deleteProductVariantOption(productId, optionId);

        return ResponseEntity.ok(FmApiResponse.success(
                "Product Variant Option deleted successfully.", null));
    }

    /**
     * DELETE /api/fm/products/updateproduct/{productId}/variant-groups/{groupId}
     *
     * Removes every variant option in the group from this product. The shared
     * variant group and group values are not deleted.
     */
    @DeleteMapping("/updateproduct/{productId}/variant-groups/{groupId}")
    public ResponseEntity<FmApiResponse<Void>> deleteProductVariantGroup(
            @PathVariable Integer productId,
            @PathVariable Integer groupId) {

        log.info("[PRODUCT] DELETE variant group. ProductId={}, GroupId={}",
                productId, groupId);

        productMappingService.deleteProductVariantGroup(productId, groupId);

        return ResponseEntity.ok(FmApiResponse.success(
                "Product Variant Group deleted successfully.", null));
    }




    /**
     * POST /api/fm/products/map-from-master-category/{outletCategoryId}
     *
     * At outlet-mapping time:
     * - Looks up the category already linked to outletCategoryId.
     * - Fetches all published master products for that category.
     * - Inserts products into jippy_fm.products.
     * - Inserts variants into jippy_fm.product_variants.
     *
     * Existing products are skipped.
     */
    // @PostMapping("/map-from-master-category/{outletCategoryId}")
    // public ResponseEntity<FmMasterProductMappingResultDTO>
    // mapFromMasterCategory(
    //         @PathVariable Integer outletCategoryId) {
    //
    //     log.info(
    //             "[PRODUCT-MAP] POST /map-from-master-category/{}",
    //             outletCategoryId
    //     );
    //
    //     return ResponseEntity.ok(
    //             productMappingService.mapFromMasterByCategory(
    //                     outletCategoryId
    //             )
    //     );
    // }

    @GetMapping("/outlet/{outletId}")
    public ResponseEntity<List<FmOutletProductResponseDto>> getProductsByOutletId(
            @PathVariable
            @NotNull(message = "Outlet id is required")
            Integer outletId) {

        log.info(
                "CONTROLLER_START | GET_PRODUCTS_BY_OUTLET | outletId={}",
                outletId
        );

        List<FmOutletProductResponseDto> response =
                productMappingService.getProductsByOutletId(outletId);

        return ResponseEntity.ok(response);
    }


    /**
     * POST /api/fm/products/bulk-upload-variants
     * <p>
     * Uploads variant Excel for a specific outlet.
     * <p>
     * Request:
     * - outletId : request parameter
     * - file     : Excel file (.xlsx / .xls)
     */
    @PostMapping(value = "/bulk-upload-variants", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FmVariantBulkUploadResponseDto> bulkUploadVariants(@RequestParam Integer outletId, @RequestParam("file") MultipartFile file) {

        log.info("[VARIANT-BULK] POST /bulk-upload-variants | outletId={} | file={}", outletId, file != null ? file.getOriginalFilename() : null);

        FmVariantBulkUploadResponseDto response = productMappingService.bulkUploadVariants(outletId, file);

        log.info("[VARIANT-BULK] COMPLETED | outletId={} | file={}", outletId, file != null ? file.getOriginalFilename() : null);

        return ResponseEntity.ok(response);
    }


    /**
     * GET /api/fm/products/{productId}
     * <p>
     * Gets product by product ID.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<FmProductUpdateResponseDto> getProductById(@PathVariable Integer productId) {

        log.info("[PRODUCT] GET Product. ProductId={}", productId);

        return ResponseEntity.ok(productMappingService.getProductById(productId));
    }


    /**
     * PUT /api/fm/products/updateCategoryAndProductDetails/{productId}
     * <p>
     * Updates category and product details.
     */
    @PutMapping("/updateCategoryAndProductDetails/{productId}")
    public ResponseEntity<FmProductUpdateResponseDto> updateProduct(@PathVariable Integer productId, @Valid @RequestBody FmProductUpdateRequestDto request) {

        log.info("[PRODUCT] UPDATE Product. ProductId={}", productId);

        return ResponseEntity.ok(productMappingService.updateProduct(productId, request));
    }


    // ==================================================================================================
    // ============================ getCategoryForProductByProductType ================================
    // ==================================================================================================

    @Operation(summary = "Get Category For Product By Product Type", description = """
            Fetch category and outlet details based on product name
            and product type.
            
            Supported product types:
            - PRODUCT
            - MASTERPRODUCT
            
            For PRODUCT:
            Returns productId, productName, outletCategoryId, outletId,
            categoryId, categoryName and outletName.
            
            For MASTERPRODUCT:
            Returns masterProductId, masterProductName, categoryId
            and categoryName.
            
            Product name matching is case-insensitive and
            leading/trailing spaces are removed before searching.
            """)
    @ApiResponses({

            @ApiResponse(responseCode = "200", description = "Details fetched successfully", content = @Content(mediaType = "application/json", examples = {

                    @ExampleObject(name = "PRODUCT Response", summary = "Response for PRODUCT type", value = """
                            [
                              {
                                "productId": 160,
                                "productName": "Lemon Soda",
                                "outletCategoryId": 80,
                                "outletId": 220,
                                "categoryId": 5,
                                "categoryName": "Mediterranean",
                                "outletName": "Sample Restaurantt"
                              }
                            ]
                            """),

                    @ExampleObject(name = "MASTERPRODUCT Response", summary = "Response for MASTERPRODUCT type", value = """
                            [
                              {
                                "masterProductId": 22,
                                "masterProductName": "Premium Cold Coffee",
                                "categoryId": 1,
                                "categoryName": "Beve"
                              },
                              {
                                "masterProductId": 24,
                                "masterProductName": "Premium Cold Coffee",
                                "categoryId": 1,
                                "categoryName": "Beve"
                              }
                            ]
                            """)})),

            @ApiResponse(responseCode = "400", description = "Invalid product name or product type", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Invalid product type. Allowed values are PRODUCT or MASTERPRODUCT.",
                      "timestamp": "2026-08-25T10:30:00"
                    }
                    """))),

            @ApiResponse(responseCode = "404", description = "Product or Master Product not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "success": false,
                      "message": "Product not found with name : ABC Product",
                      "timestamp": "2026-08-25T10:30:00"
                    }
                    """)))})
    @GetMapping("/getCategoryForProductByProductType")
    public ResponseEntity<FmApiResponse<Object>> getCategoryForProductByProductType(

            @Parameter(description = "Product name to search", required = true, example = "Lemon Soda", schema = @Schema(type = "string", example = "Lemon Soda")) @NotBlank(message = "Product name is required.") @RequestParam String productName,

            @Parameter(description = "Product type. Allowed values: PRODUCT or MASTERPRODUCT", required = true, example = "PRODUCT", schema = @Schema(type = "string", allowableValues = {"PRODUCT", "MASTERPRODUCT"}, example = "PRODUCT")) @Pattern(regexp = "PRODUCT|MASTERPRODUCT", message = "Invalid product type. Allowed values are PRODUCT or MASTERPRODUCT.") @RequestParam String productType) {

        log.info("[GET_CATEGORY_FOR_PRODUCT] Controller request | productName={} | productType={}", productName, productType);

        Object response = productMappingService.getCategoryForProductByProductType(productName, productType);

        return ResponseEntity.ok(FmApiResponse.success("Category details fetched successfully.", response));
    }


    // =====================================================================================================
    // ========================== updateCategoryForProductByProductType ===================================
    // =====================================================================================================

    @PutMapping("/updateCategoryForProductByProductType")
    @Operation(summary = "Update Category For Product By Product Type", description = """
            Updates the category for a Product or Master Product.
            
            PRODUCT:
            1. Finds the product using productName.
            2. Gets the outletCategoryId from products.
            3. Updates category_id in outlet_categories.
            
            MASTERPRODUCT:
            1. Finds all master products using masterProductName.
            2. Updates category_id in all matching master_products records.
            
            Supported product types:
            - PRODUCT
            - MASTERPRODUCT
            """)
    @ApiResponses({

            @ApiResponse(responseCode = "200", description = "Category updated successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid request"),

            @ApiResponse(responseCode = "404", description = "Product, Master Product or Category not found")})
    public ResponseEntity<FmApiResponse<FmProductCategoryUpdateResponseDto>> updateCategoryForProductByProductType(@Valid @RequestBody FmProductCategoryUpdateRequestDto request) {

        log.info("[UPDATE_CATEGORY_FOR_PRODUCT] Controller request | productName={} | productType={} | updatedCategoryId={}", request.getProductName(), request.getProductType(), request.getUpdatedCategoryId());

        FmProductCategoryUpdateResponseDto response = productMappingService.updateCategoryForProductByProductType(request);

        return ResponseEntity.ok(FmApiResponse.success("Category updated successfully.", response));
    }


    // =================================================================================================
    // ================================= PRODUCT VALIDATION ===========================================
    // =================================================================================================

    /**
     * Checks whether a product belongs to an outlet.
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsProductInOutlet(@RequestParam Integer outletId, @RequestParam Integer productId) {

        log.info("Received request to validate product belongs to outlet. " + "outletId={}, productId={}", outletId, productId);

        Boolean exists = productMappingService.existsProductInOutlet(outletId, productId);

        log.info("Product validation request completed. " + "outletId={}, productId={}, exists={}", outletId, productId, exists);

        return ResponseEntity.ok(exists);
    }


    /**
     * Gets active product IDs for an outlet.
     */
    @GetMapping("/active-product-ids")
    public ResponseEntity<List<Integer>> getActiveProductIdsByOutlet(@RequestParam Integer outletId) {

        log.info("Received request to fetch active product ids. outletId={}", outletId);

        List<Integer> productIds = productMappingService.getActiveProductIdsByOutlet(outletId);

        log.info("Returning {} active product ids. outletId={}", productIds.size(), outletId);

        return ResponseEntity.ok(productIds);
    }


    // =================================================================================================
    // ================================= OUTLET PRODUCT PRICING =======================================
    // =================================================================================================

    /**
     * GET /api/fm/products/outlets/{outletId}/pricing
     * <p>
     * Returns product pricing for an outlet.
     */
    @GetMapping("/outlets/{outletId}/pricing")
    public ResponseEntity<List<OutletProductPricingDto>> getProductPricingByOutletId(@PathVariable Integer outletId) {

        log.info("GET_PRODUCT_PRICING_BY_OUTLET_START | outletId={}", outletId);

        List<OutletProductPricingDto> products = productMappingService.getProductPricingByOutletId(outletId);

        log.info("GET_PRODUCT_PRICING_BY_OUTLET_SUCCESS | " + "outletId={} | productCount={}", outletId, products.size());

        return ResponseEntity.ok(products);
    }
    // ================================= PRODUCT DETAILS ===============================================

    /**
     * GET /api/fm/products/productdetails/{productId}
     * <p>
     * Returns complete product details including:
     * - Product information
     * - Variant groups
     * - Available variant options
     */
    @GetMapping("/productdetails/{productId}")
    public ResponseEntity<FmProductDetailResponse> getProductDetailById(@PathVariable Integer productId) {

        log.info("[PRODUCT] GET_PRODUCT_DETAIL | productId={}", productId);

        FmProductDetailResponse response = productMappingService.getProductDetailById(productId);

        log.info("[PRODUCT] GET_PRODUCT_DETAIL_SUCCESS | productId={}", productId);

        return ResponseEntity.ok(response);
    }
    // ================================= MERCHANT PRICE UPDATE =========================================
    @PutMapping("/{productId}/merchant-price")
    @Operation(summary = "Update merchant price", description = """
            Updates merchant price for a product.
            
            ROLE_MERCHANT:
            - Can decrease merchant price only.
            
            ROLE_SUPERADMIN:
            - Can increase or decrease merchant price.
            
            ROLE_DEVADMIN:
            - Can increase or decrease merchant price.
            
            Every successful price change is stored
            in merchant_price_change_history.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merchant price updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid request or merchant attempted to increase price"), @ApiResponse(responseCode = "404", description = "Product or outlet not found")})
    public ResponseEntity<FmMerchantPriceUpdateResponse> updateMerchantPrice(@PathVariable Integer productId, @Valid @RequestBody FmMerchantPriceUpdateRequest request) {

        log.info("[MERCHANT-PRICE] PUT /{}/merchant-price | price={} | role={} | updatedBy={}", productId, request.getMerchantPrice(), request.getRole(), request.getUpdatedBy());

        FmMerchantPriceUpdateResponse response = productMappingService.updateMerchantPrice(productId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getOrderProductItemsForMerchant")
    public ResponseEntity<List<FmOrderItemsEvent>> getOrderProductItemsForMerchant(@RequestParam List<Integer> productIds,
            @RequestParam List<Integer> productVariantIds) {

        log.info("Get order product items for merchant  | productIds:{} , product variant ids:{} ", productIds,productVariantIds);

        List<FmOrderItemsEvent> products = productMappingService.getOrderProductItemsForMerchant(productIds,productVariantIds);

        return ResponseEntity.ok(products);
    }
}