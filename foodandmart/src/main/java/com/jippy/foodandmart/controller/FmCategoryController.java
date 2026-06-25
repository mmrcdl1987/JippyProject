package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.entity.FmOutletCategory;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.repository.FmOutletCategoryRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.serviceImpl.FmMasterProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for global category management and outlet-category linking.
 *
 * <p>Why categories are split from products: a category is a shared global
 * entity (e.g. "Beverages") that many outlets can reference. The
 * outlet_categories join table records which categories an outlet has activated.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
public class FmCategoryController {

    private final FmCategoryRepository categoryRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmOutletRepository outletRepository;


    /**
     * Creates an outlet-category link if it doesn't already exist.
     *
     * <p>POST /api/outlet-categories</p>
     *
     * <p>Why return existing if already linked: the UI can call this endpoint
     * idempotently. Re-linking an already-linked category returns 200 with the
     * existing record's ID instead of throwing a 409 conflict.</p>
     *
     * <p>Request body: {@code { "outletId": 1, "categoryId": 5 }}</p>
     *
     * @param body JSON body with integer outletId and categoryId fields
     * @return 201 on creation, 200 if already exists, 400/404 on validation errors
     */
    @PostMapping("/api/outlet-categories")
    public ResponseEntity<FmApiResponse<Map<String, Integer>>> createOutletCategory(
            @RequestBody Map<String, Integer> body) {

        Integer outletId   = body.get("outletId");
        Integer categoryId = body.get("categoryId");

        log.info("[CATEGORY] POST /api/outlet-categories — outletId={} categoryId={}", outletId, categoryId);

        if (outletId == null || categoryId == null)
            return ResponseEntity.badRequest()
                    .body(FmApiResponse.error("outletId and categoryId are required"));

        if (!outletRepository.existsById(outletId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(FmApiResponse.error("Outlet ID " + outletId + " not found"));

        if (!categoryRepository.existsById(categoryId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(FmApiResponse.error("Category ID " + categoryId + " not found"));

        // Idempotent: return existing link instead of throwing a duplicate error
        FmOutletCategory existing = outletCategoryRepository
                .findByOutletIdAndCategoryId(outletId, categoryId)
                .orElse(null);
        if (existing != null) {
            log.info("[CATEGORY] Already exists outletCategoryId={}", existing.getOutletCategoryId());
            return ResponseEntity.ok(FmApiResponse.success(
                    "Already exists", Map.of("outletCategoryId", existing.getOutletCategoryId())));
        }

        // Create new outlet-category link using setter methods
        FmOutletCategory oc = new FmOutletCategory();
        oc.setOutletId(outletId);
        oc.setCategoryId(categoryId);
        FmOutletCategory saved = outletCategoryRepository.save(oc);
        log.info("[CATEGORY] Created outletCategoryId={}", saved.getOutletCategoryId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FmApiResponse.success("Created", Map.of("outletCategoryId", saved.getOutletCategoryId())));
    }
}
