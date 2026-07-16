package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmProductVariantGroupRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantGroupResponseDto;
import com.jippy.foodandmart.service.IFmProductVariantGroupService;
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
public class FmProductVariantGroupController {

    private final IFmProductVariantGroupService service;

    /**
     * Create / Update Variant Group
     */
    @PostMapping
    public ResponseEntity<FmProductVariantGroupResponseDto> saveVariantGroup(
            @Valid @RequestBody FmProductVariantGroupRequestDto request) {

        log.info("Received save variant group request. GroupId={}, GroupName={}",
                request.getProductVariantGroupsId(),
                request.getGroupName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.saveVariantGroup(request));
    }

    /**
     * Get All Variant Groups
     */
    @GetMapping
    public ResponseEntity<List<FmProductVariantGroupResponseDto>> getAllVariantGroups() {

        log.info("Received request to fetch all product variant groups.");

        return ResponseEntity.ok(service.getAllVariantGroups());
    }

    /**
     * Get Variant Group By Id
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<FmProductVariantGroupResponseDto> getVariantGroupById(
            @PathVariable Integer groupId) {

        log.info("Received request to fetch product variant group. GroupId={}", groupId);

        return ResponseEntity.ok(service.getVariantGroupById(groupId));
    }

    /**
     * Soft Delete Variant Group
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<FmApiResponse<Void>> deleteVariantGroup(
            @PathVariable Integer groupId) {

        log.info("Received request to delete product variant group. GroupId={}", groupId);

        service.deleteVariantGroup(groupId);

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Product Variant Group deleted successfully.",
                        null));
    }
}