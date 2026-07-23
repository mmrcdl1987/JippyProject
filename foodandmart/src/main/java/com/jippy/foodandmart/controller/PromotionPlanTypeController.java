package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.PromotionPlanTypeAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeResponseDto;
import com.jippy.foodandmart.service.IPromotionPlanTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/promotion-plan-types")
@RequiredArgsConstructor
@Slf4j
public class PromotionPlanTypeController {

    private final IPromotionPlanTypeService promotionPlanTypeService;

    /**
     * Create Promotion Plan Type
     */
    @PostMapping
    public ResponseEntity<PromotionPlanTypeAuditResponseDto> createPromotionPlanType(
            @Valid @RequestBody PromotionPlanTypeRequestDto requestDto) {

        log.info("Received request to create Promotion Plan Type");

        PromotionPlanTypeAuditResponseDto response =
                promotionPlanTypeService.createPromotionPlanType(requestDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get Promotion Plan Type By Id
     */
    @GetMapping("/{promotionPlanTypeId}")
    public ResponseEntity<PromotionPlanTypeResponseDto> getPromotionPlanTypeById(
            @PathVariable Integer promotionPlanTypeId) {

        log.info("Received request to fetch Promotion Plan Type : {}", promotionPlanTypeId);

        return ResponseEntity.ok(
                promotionPlanTypeService.getPromotionPlanTypeById(promotionPlanTypeId));
    }

    /**
     * Get All Promotion Plan Types
     */
    @GetMapping
    public ResponseEntity<List<PromotionPlanTypeResponseDto>> getAllPromotionPlanTypes() {

        log.info("Received request to fetch all Promotion Plan Types");

        return ResponseEntity.ok(
                promotionPlanTypeService.getAllPromotionPlanTypes());
    }

    /**
     * Update Promotion Plan Type
     */
    @PutMapping("/{promotionPlanTypeId}")
    public ResponseEntity<PromotionPlanTypeAuditResponseDto> updatePromotionPlanType(
            @PathVariable Integer promotionPlanTypeId,
            @Valid @RequestBody PromotionPlanTypeRequestDto requestDto) {

        log.info("Received request to update Promotion Plan Type : {}", promotionPlanTypeId);

        return ResponseEntity.ok(
                promotionPlanTypeService.updatePromotionPlanType(
                        promotionPlanTypeId,
                        requestDto));
    }

    /**
     * Delete Promotion Plan Type
     */
    @DeleteMapping("/{promotionPlanTypeId}")
    public ResponseEntity<String> deletePromotionPlanType(
            @PathVariable Integer promotionPlanTypeId) {

        log.info("Received request to delete Promotion Plan Type : {}", promotionPlanTypeId);

        promotionPlanTypeService.deletePromotionPlanType(promotionPlanTypeId);

        return ResponseEntity.ok("Promotion Plan Type deleted successfully.");
    }
}