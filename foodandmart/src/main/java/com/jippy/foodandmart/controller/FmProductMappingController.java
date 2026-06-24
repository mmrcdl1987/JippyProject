package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmMapToProductRequest;
import com.jippy.foodandmart.dto.FmMapToProductResult;
import com.jippy.foodandmart.dto.FmMasterProductMappingResultDTO;
import com.jippy.foodandmart.service.IFmProductMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/fm/products")
@RequiredArgsConstructor
public class FmProductMappingController {

    private final IFmProductMappingService productMappingService;


    /**
     * POST /api/products/from-master
     * Maps selected products (from compare result) into jippy_fm.products
     * linked to the chosen outlet category.
     */
    @PostMapping("/from-master")
    public ResponseEntity<FmMapToProductResult> mapFromMaster(@RequestBody FmMapToProductRequest req) {
        log.info("[PRODUCT-MAP] POST /api/products/from-master — outletCategoryId={}, count={}",
                req.getOutletCategoryId(),
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
    @PostMapping("/map-from-master-category/{outletCategoryId}")
    public ResponseEntity<FmMasterProductMappingResultDTO> mapFromMasterCategory(
            @PathVariable Integer outletCategoryId) {

        log.info("[PRODUCT-MAP] POST /api/products/map-from-master-category/{}", outletCategoryId);
        return ResponseEntity.ok(productMappingService.mapFromMasterByCategory(outletCategoryId));
    }
}
