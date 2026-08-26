package com.jippy.division.feignClient;

import com.jippy.division.config.FeignClientConfig;
import com.jippy.division.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "foodandmart",
        configuration = FeignClientConfig.class
)
public interface FMFeignClient {

    // ============================================================
    // OUTLET APIs
    // ============================================================

    @GetMapping("/api/fm/outlets/merchant/{merchantId}")
    DivFmApiResponse<List<DivFmOutletDto>> getOutletsByMerchantId(
            @PathVariable("merchantId") Integer merchantId
    );

    @GetMapping("/api/outlets/available-outlets/{areaId}")
    List<DivOutletDto> getOutletsByAreaId(
            @PathVariable("areaId") Integer areaId
    );

    @GetMapping("/api/fm/outlets/getOutletDetails")
    DivOutletDetailsDto getOutletDetails(
            @RequestParam("outletId") Integer outletId,
            @RequestParam("userType") String userType,
            @RequestParam(value = "customerId", required = false) Integer customerId
    );

    /**
     * Fetch outlet address details.
     *
     * Endpoint:
     * GET /api/fm/outlets/getOutletAddressDetails?outletId={outletId}
     */
    @GetMapping("/api/fm/outlets/getOutletAddressDetails")
    DivOutletDetailsDto getOutletAddressDetails(
            @RequestParam("outletId") Integer outletId
    );


    // ============================================================
    // CAMPAIGN / MEAL TYPE APIs
    // ============================================================

    @GetMapping("/api/fm/campaign/all")
    List<DivMealTypeTimingResponseDto> getAllMealTypeTimings();

    @GetMapping("/api/fm/campaign/{mealTypeTimingsId}/exists")
    Boolean existsMealTypeTiming(
            @PathVariable("mealTypeTimingsId") Integer mealTypeTimingsId
    );


    // ============================================================
    // PROMOTION APIs
    // ============================================================

    @GetMapping(
            "/api/fm/internal/promotion-plans/{promotionPlanId}/schedule-details"
    )
    PromotionScheduleDetailsDto getPromotionScheduleDetails(
            @PathVariable("promotionPlanId") Integer promotionPlanId
    );


    // ============================================================
    // PRODUCT APIs
    // ============================================================

    @GetMapping("/api/fm/products/exists")
    Boolean existsProductInOutlet(
            @RequestParam("outletId") Integer outletId,
            @RequestParam("productId") Integer productId
    );

    @GetMapping("/api/fm/products/active-product-ids")
    List<Integer> getActiveProductIdsByOutlet(
            @RequestParam("outletId") Integer outletId
    );
}