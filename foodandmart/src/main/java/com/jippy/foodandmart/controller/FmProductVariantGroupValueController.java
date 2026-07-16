package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmProductVariantGroupValueResponseDto;
import com.jippy.foodandmart.dto.FmProductVariantValueRequestDto;
import com.jippy.foodandmart.service.IFmProductVariantGroupValueService;
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
@RequestMapping("/api/fm/product-variant-groups")
public class FmProductVariantGroupValueController {

    private final IFmProductVariantGroupValueService service;

    /**
     * Create / Update Variant Value
     */
    @PostMapping("/{groupId}/values")
    public ResponseEntity<FmProductVariantGroupValueResponseDto> saveVariantGroupValue(
            @PathVariable Integer groupId,
            @Valid @RequestBody FmProductVariantValueRequestDto request) {

        log.info("Received save variant value request. GroupId={}, ValueId={}, VariantName={}",
                groupId,
                request.getProductVariantGroupValuesId(),
                request.getVariantName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.saveVariantGroupValue(groupId, request));
    }

    /**
     * Get All Variant Values
     */
    @GetMapping("/{groupId}/values")
    public ResponseEntity<List<FmProductVariantGroupValueResponseDto>> getVariantGroupValues(
            @PathVariable Integer groupId) {

        log.info("Received request to fetch variant values. GroupId={}", groupId);

        return ResponseEntity.ok(
                service.getVariantGroupValues(groupId));
    }

    /**
     * Get Variant Value By Id
     */
    @GetMapping("/{groupId}/values/{valueId}")
    public ResponseEntity<FmProductVariantGroupValueResponseDto> getVariantGroupValueById(
            @PathVariable Integer groupId,
            @PathVariable Integer valueId) {

        log.info("Received request to fetch variant value. GroupId={}, ValueId={}",
                groupId,
                valueId);

        return ResponseEntity.ok(
                service.getVariantGroupValueById(groupId, valueId));
    }

    /**
     * Soft Delete Variant Value
     */
    @DeleteMapping("/{groupId}/values/{valueId}")
    public ResponseEntity<FmApiResponse<Void>> deleteVariantGroupValue(
            @PathVariable Integer groupId,
            @PathVariable Integer valueId) {

        log.info("Received request to delete variant value. GroupId={}, ValueId={}",
                groupId,
                valueId);

        service.deleteVariantGroupValue(groupId, valueId);

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Product Variant Value deleted successfully.",
                        null));
    }
}