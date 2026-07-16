package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmProductVariantOptionRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantOptionResponseDto;
import com.jippy.foodandmart.service.IFmProductVariantOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm/products")
public class FmProductVariantOptionController {

    private final IFmProductVariantOptionService service;

    /**
     * Create / Update Product Variant Option
     */
    @PostMapping("/{productId}/variant-options")
    public ResponseEntity<FmProductVariantOptionResponseDto> saveProductVariantOption(
            @PathVariable Integer productId,
            @Valid @RequestBody FmProductVariantOptionRequestDto request) {

        log.info("Received save product variant option request. ProductId={}, OptionId={}, VariantValueId={}",
                productId,
                request.getProductVariantOptionsId(),
                request.getProductVariantGroupValuesId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.saveProductVariantOption(productId, request));
    }

    /**
     * Get All Product Variant Options
     */
    @GetMapping("/{productId}/variant-options")
    public ResponseEntity<List<FmProductVariantOptionResponseDto>> getProductVariantOptions(
            @PathVariable Integer productId) {

        log.info("Received request to fetch product variant options. ProductId={}",
                productId);

        return ResponseEntity.ok(
                service.getProductVariantOptions(productId));
    }

    /**
     * Get Product Variant Option By Id
     */
    @GetMapping("/{productId}/variant-options/{optionId}")
    public ResponseEntity<FmProductVariantOptionResponseDto> getProductVariantOptionById(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        log.info("Received request to fetch product variant option. ProductId={}, OptionId={}",
                productId,
                optionId);

        return ResponseEntity.ok(
                service.getProductVariantOptionById(productId, optionId));
    }

    /**
     * Soft Delete Product Variant Option
     */
    @DeleteMapping("/{productId}/variant-options/{optionId}")
    public ResponseEntity<FmApiResponse<Void>> deleteProductVariantOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        log.info("Received request to delete product variant option. ProductId={}, OptionId={}",
                productId,
                optionId);

        service.deleteProductVariantOption(productId, optionId);

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Product Variant Option deleted successfully.",
                        null));
    }
}