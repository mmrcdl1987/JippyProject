package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.dto.FmProductPriceSettingsResponseDto;
import com.jippy.foodandmart.dto.FmResponseDto;
import com.jippy.foodandmart.service.IFmProductPriceSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/product-price-settings")
@RequiredArgsConstructor
@Slf4j
public class FmProductPriceSettingsController {

    private final IFmProductPriceSettingsService priceSettingsService;

    @PostMapping
    public ResponseEntity<FmProductPriceSettingsResponseDto> create(@Valid @RequestBody FmProductPriceSettingsRequestDto request) {

        log.info("API START | CREATE_PRODUCT_PRICE_SETTING | outletId={} | productId={} | variantId={}", request.getOutletId(), request.getProductId(), request.getProductVariantId());

        // TODO: Replace with authenticated user ID from JWT/SecurityContext
        Integer userId = 1;

        FmProductPriceSettingsResponseDto response = priceSettingsService.create(request, userId);

        log.info("API END | CREATE_PRODUCT_PRICE_SETTING | settingId={}", response.getProductPriceSettingsId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FmProductPriceSettingsResponseDto> getById(@PathVariable Integer id) {

        log.info("API START | GET_PRODUCT_PRICE_SETTING | settingId={}", id);

        FmProductPriceSettingsResponseDto response = priceSettingsService.getById(id);

        log.info("API END | GET_PRODUCT_PRICE_SETTING | settingId={}", id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<FmProductPriceSettingsResponseDto>> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        log.info("API START | GET_ALL_PRODUCT_PRICE_SETTINGS | page={} | size={}", page, size);

        Page<FmProductPriceSettingsResponseDto> response = priceSettingsService.getAll(page, size);

        log.info("API END | GET_ALL_PRODUCT_PRICE_SETTINGS | page={} | size={} | returnedElements={} | totalElements={}", page, size, response.getNumberOfElements(), response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FmProductPriceSettingsResponseDto> update(@PathVariable Integer id, @Valid @RequestBody FmProductPriceSettingsRequestDto request) {

        log.info("API START | UPDATE_PRODUCT_PRICE_SETTING | settingId={}", id);

        // TODO: Replace with authenticated user ID from JWT/SecurityContext
        Integer userId = 1;

        FmProductPriceSettingsResponseDto response = priceSettingsService.update(id, request, userId);

        log.info("API END | UPDATE_PRODUCT_PRICE_SETTING | settingId={}", id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FmResponseDto> delete(@PathVariable Integer id) {

        log.info("API START | DELETE_PRODUCT_PRICE_SETTING | settingId={}", id);

        priceSettingsService.delete(id);

        log.info("API END | DELETE_PRODUCT_PRICE_SETTING | settingId={}", id);

        return ResponseEntity.ok(new FmResponseDto(FmAppConstants.STATUS_200, "Product price setting deleted successfully"));
    }
}