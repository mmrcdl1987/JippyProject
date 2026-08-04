package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.FmProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/fm/products")
@RequiredArgsConstructor
public class    FmProductController {

    private final FmProductService productMappingService;


    /**
     * POST /api/products/from-master
     * Maps selected master products into outlet products.
     */
    @PostMapping("/from-master")
    public ResponseEntity<FmMapToProductResult> mapFromMaster(
            @RequestBody FmMapToProduct req) {

        log.info(
                "[PRODUCT-MAP] POST /from-master - OutletId={}, CategoryId={}, Products={}",
                req.getOutletId(),
                req.getCategoryId(),
                req.getProducts() == null ? 0 : req.getProducts().size());

        return ResponseEntity.ok(productMappingService.mapToProducts(req));
    }

    /**
     * POST /api/products/map-from-master-category/{outletCategoryId}
     *
     * At outlet-mapping time: looks up the category already linked to
     * outletCategoryId, fetches ALL published master_products for that
     * category, and auto-inserts them into:
     *   → jippy_fm.products          (one row per master product)
     *   → jippy_fm.product_variants  (one row per option/variant from options jsonb)
     *
     * Already-existing products for this outlet category are skipped.
     */
//    @PostMapping("/map-from-master-category/{outletCategoryId}")
//    public ResponseEntity<FmMasterProductMappingResultDTO> mapFromMasterCategory(
//            @PathVariable Integer outletCategoryId) {
//
//        log.info("[PRODUCT-MAP] POST /map-from-master-category/{}",
//                outletCategoryId);
//
//        return ResponseEntity.ok(
//                productMappingService.mapFromMasterByCategory(outletCategoryId));
//    }

    /**
     * GET /api/fm/products/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<FmProductUpdateResponseDto> getProductById(
            @PathVariable Integer productId) {

        log.info("[PRODUCT] GET Product. ProductId={}", productId);

        return ResponseEntity.ok(
                productMappingService.getProductById(productId));
    }

    /**
     * PUT /api/fm/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<FmProductUpdateResponseDto> updateProduct(
            @PathVariable Integer productId,
            @Valid @RequestBody FmProductUpdateRequestDto request) {

        log.info("[PRODUCT] UPDATE Product. ProductId={}", productId);

        return ResponseEntity.ok(
                productMappingService.updateProduct(productId, request));
    }

}
