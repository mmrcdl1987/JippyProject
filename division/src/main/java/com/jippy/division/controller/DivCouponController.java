package com.jippy.division.controller;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.*;
import com.jippy.division.service.ICouponService;
import com.jippy.division.service.IDivCampaignService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/div/coupons")
@Tag(
        name = "Coupons API",
        description = "Rest API to create and perform operations on coupons")
public class DivCouponController {

    @Autowired
    ICouponService couponService;

    @Autowired
    IDivCampaignService divCampaignService;


    private static final Logger logger = LoggerFactory.getLogger(DivCouponController.class);

    /**
     * API to create a new coupon
     */
    @PostMapping
    public ResponseEntity<DivResponseDto> createCoupon(@Valid @RequestBody DivCouponRequestDto couponRequestDto) {

        logger.info("API createCoupon initiated for code={}", couponRequestDto.getCouponCode());

        couponService.createCoupon(couponRequestDto);

        logger.info("API createCoupon success for code={}", couponRequestDto.getCouponCode());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DivResponseDto(DivAppConstants.STATUS_201, "Coupon created successfully"));
    }

    /**
     * API to update existing coupon
     */
    @PutMapping("/{id}")
    public ResponseEntity<DivResponseDto> updateCoupon(@Valid @RequestBody DivCouponRequestDto divCouponRequestDto) {

        logger.info("API updateCoupon initiated id={}",divCouponRequestDto.getCouponId());

        couponService.updateCoupon(divCouponRequestDto);

        logger.info("API updateCoupon success id={}", divCouponRequestDto.getCouponId());

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, "Coupon updated successfully"));
    }

    /**
     * API to disable coupon
     */
    @PatchMapping("/disable/{couponId}")
    public ResponseEntity<DivResponseDto> disableCoupon(@PathVariable Integer couponId) {

        logger.info("API disableCoupon initiated id={}", couponId);

        couponService.disableCoupon(couponId);

        logger.info("API disableCoupon success id={}", couponId);

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, "Coupon disabled"));
    }
    /**
     * API to enable coupon
     */

    @PatchMapping("/enable/{couponId}")
    public ResponseEntity<DivResponseDto> enableCoupon(@PathVariable Integer couponId) {

        logger.info("API enableCoupon initiated id={}", couponId);

        couponService.enableCoupon(couponId);

        logger.info("API enableCoupon success id={}", couponId);

        return ResponseEntity.ok(
                new DivResponseDto(DivAppConstants.STATUS_200, "Coupon enabled"));
    }

    /**
     * API to fetch all coupons
     */

    @GetMapping
    public ResponseEntity<List<DivCouponResponseDto>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("API getAllCoupons initiated");

        List<DivCouponResponseDto> coupons = couponService.getAllCoupons(page, size);

        logger.info("API getAllCoupons success, count={}", coupons.size());

        return ResponseEntity.ok(coupons);
    }


    @GetMapping(value = "/getPriceModels",produces = "application/json")
    public ResponseEntity<List<DivPriceModelDto>> getPriceModels(){
        logger.info("getPriceModels API initiated");
        List<DivPriceModelDto> priceModelDtoList =couponService.getAllPriceModels();
        logger.info("getPriceModels API Response : {}", priceModelDtoList.toString());
        return  ResponseEntity.status(HttpStatus.OK).body(priceModelDtoList);
    }
    /**
     * CREATE CAMPAIGN
     */
    @PostMapping("/campaign/create")
    public ResponseEntity<DivResponseDto> createCampaign(@RequestBody DivCampaignRequestDto dto) {

        logger.info("API createCampaign initiated");

        // CAMPAIGN TYPE REQUIRED

        if (dto.getCampainType() == null || dto.getCampainType().isBlank()) {

            throw new RuntimeException("campainType is required");
        }

        // COUPON VALIDATION

        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

            if (dto.getCouponId() == null) {

                throw new RuntimeException("couponId is required " + "for COUPON campaign");
            }
        }

        // PRICE DROP VALIDATION

        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

            // PRICE MODEL REQUIRED

            if (dto.getPriceModelId() == null) {

                throw new RuntimeException("priceModelId is required");
            }

            // ONLY 1 OR 2

            if (!(dto.getPriceModelId() == 1 || dto.getPriceModelId() == 2)) {

                throw new RuntimeException("priceModelId must be " + "1 (FLAT) or 2 (PERCENTAGE)");
            }

            // FLAT VALIDATION

            if (dto.getPriceModelId() == 1 && dto.getPriceDropValue() == null) {

                throw new RuntimeException("priceDropValue required " + "for FLAT");
            }
        }

        // OUTLET VALIDATION

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new RuntimeException("outletIds required");
        }


//        if (dto.getProductIds() == null || dto.getProductIds().isEmpty()) {
//
//            throw new RuntimeException("productIds required");
//        }

        // SLOT VALIDATION

        if (dto.getSlots() == null || dto.getSlots().isEmpty()) {

            throw new RuntimeException("slots required");
        }

        String response = divCampaignService.createCampaign(dto);

        logger.info("Campaign created successfully");

        return ResponseEntity.ok(new DivResponseDto(DivAppConstants.STATUS_200, response));
    }

    // FILE: controller/DivCouponController.java
    @GetMapping("/available-outlets/{areaId}")
    public ResponseEntity<List<DivOutletDto>> getAvailableOutlets(@PathVariable Integer areaId) {

        logger.info("API getAvailableOutlets initiated areaId={}", areaId);

        List<DivOutletDto> outlets = divCampaignService.getAvailableOutlets(areaId);

        return ResponseEntity.ok(outlets);
    }

}
