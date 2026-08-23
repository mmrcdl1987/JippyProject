package com.jippy.division.controller;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivActiveDiscountsResponseDto;
import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.dto.DivOutletDto;
import com.jippy.division.dto.DivResponseDto;
import com.jippy.division.service.IDivCampaignService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/div/campaign")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign API", description = "REST APIs to create and manage Coupon and Price Drop campaigns")
public class DivCampaignController {

    private final IDivCampaignService divCampaignService;

    /**
     * Create Campaign
     */
    @PostMapping("/campaign/create")
    public ResponseEntity<DivResponseDto> createCampaign(@Valid @RequestBody DivCampaignRequestDto dto) {

        log.info("Campaign Create API Started");

        String response = divCampaignService.createCampaign(dto);

        log.info("Campaign Create API Completed");

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, response));
    }

    /**
     * Update Campaign
     */
    @PutMapping("/campaign/{campaignId}")
    public ResponseEntity<DivResponseDto> updateCampaign(@PathVariable Integer campaignId, @Valid @RequestBody DivCampaignRequestDto dto) {

        log.info("Campaign Update API Started. campaignId={}", campaignId);

        String response = divCampaignService.updateCampaign(campaignId, dto);

        log.info("Campaign Update API Completed. campaignId={}", campaignId);

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, response));
    }

    /**
     * Delete Campaign
     */
    @DeleteMapping("/campaign/{campaignType}/{campaignId}")
    public ResponseEntity<DivResponseDto> deleteCampaign(@PathVariable String campaignType, @PathVariable Integer campaignId) {

        log.info("Campaign Delete API Started. campaignType={}, campaignId={}", campaignType, campaignId);

        String response = divCampaignService.deleteCampaign(campaignType, campaignId);

        log.info("Campaign Delete API Completed. campaignType={}, campaignId={}", campaignType, campaignId);

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, response));
    }

    /**
     * Get Available Outlets
     */
    @GetMapping("/available-outlets/{areaId}")
    public ResponseEntity<List<DivOutletDto>> getAvailableOutlets(@PathVariable Integer areaId) {

        log.info("Get Available Outlets API Started. areaId={}", areaId);

        List<DivOutletDto> outlets = divCampaignService.getAvailableOutlets(areaId);

        log.info("Get Available Outlets API Completed. count={}", outlets.size());

        return ResponseEntity.ok(outlets);
    }

    /**
     * Get Active Discounts
     */
    @GetMapping("/getActiveDiscounts")
    public ResponseEntity<List<DivActiveDiscountsResponseDto>> getActiveDiscounts() {

        log.info("Get Active Discounts API Started");

        List<DivActiveDiscountsResponseDto> activeDiscounts = divCampaignService.getActiveDiscounts();

        log.info("Get Active Discounts API Completed. count={}", activeDiscounts.size());

        return ResponseEntity.ok(activeDiscounts);
    }
}