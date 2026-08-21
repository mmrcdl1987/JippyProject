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
    @PutMapping("/{couponId}")
    public ResponseEntity<DivResponseDto> updateCoupon(
            @PathVariable Integer couponId,
            @Valid @RequestBody DivCouponRequestDto divCouponRequestDto) {

        divCouponRequestDto.setCouponId(couponId);

        logger.info("API updateCoupon initiated. CouponId={}", couponId);

        couponService.updateCoupon(divCouponRequestDto);

        logger.info("API updateCoupon completed successfully. CouponId={}", couponId);

        return ResponseEntity.ok(
                new DivResponseDto(
                        DivAppConstants.STATUS_200,
                        "Coupon updated successfully"));
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

    @GetMapping("/{couponId}")
    public ResponseEntity<DivCouponResponseDto> getCouponById(
            @PathVariable Integer couponId) {

        logger.info("API getCouponById initiated. CouponId={}", couponId);

        DivCouponResponseDto coupon = couponService.getCouponById(couponId);

        logger.info("API getCouponById completed successfully. CouponId={}", couponId);

        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/welcome")
    public ResponseEntity<List<DivCouponResponseDto>> getActiveWelcomeCoupons() {

        return ResponseEntity.ok(
                couponService.getActiveWelcomeCoupons());

    }


    @GetMapping(value = "/getPriceModels",produces = "application/json")
    public ResponseEntity<List<DivPriceModelDto>> getPriceModels(){
        logger.info("getPriceModels API initiated");
        List<DivPriceModelDto> priceModelDtoList =couponService.getAllPriceModels();
        logger.info("getPriceModels API Response : {}", priceModelDtoList.toString());
        return  ResponseEntity.status(HttpStatus.OK).body(priceModelDtoList);
    }
//    /**
//     * CREATE CAMPAIGN
//     */
//    @PostMapping("/campaign/create")
//    public ResponseEntity<DivResponseDto> createCampaign(
//            @RequestBody DivCampaignRequestDto dto) {
//
//        logger.info("Campaign Create API Started");
//
//        String response = divCampaignService.createCampaign(dto);
//
//        logger.info("Campaign Create API Completed");
//
//        return ResponseEntity.ok(
//                new DivResponseDto(
//                        DivAppConstants.STATUS_200,
//                        response));
//    }

    // FILE: controller/DivCouponController.java
    @GetMapping("/available-outlets/{areaId}")
    public ResponseEntity<List<DivOutletDto>> getAvailableOutlets(@PathVariable Integer areaId) {

        logger.info("API getAvailableOutlets initiated areaId={}", areaId);

        List<DivOutletDto> outlets = divCampaignService.getAvailableOutlets(areaId);

        return ResponseEntity.ok(outlets);
    }
    @PostMapping("/available-meal-slots")
    public ResponseEntity<List<AvailableMealSlotResponseDto>> getAvailableMealSlots(
            @RequestBody AvailableMealSlotRequestDto request) {

        logger.info("Fetching available meal slots");

        return ResponseEntity.ok(
                divCampaignService.getAvailableMealSlots(request));
    }
    @GetMapping("/active")
    public ResponseEntity<List<DivCouponResponseDto>> getActiveCoupons() {
        logger.info("REST Request: GET /api/div/coupons/active");
        List<DivCouponResponseDto> activeCoupons = couponService.getAllActiveCoupons();
        return ResponseEntity.ok(activeCoupons);
    }

}
