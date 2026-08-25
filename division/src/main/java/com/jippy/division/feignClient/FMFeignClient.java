package com.jippy.division.feignClients;

import com.jippy.division.config.FeignClientConfig;
import com.jippy.division.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "foodandmart", configuration = FeignClientConfig.class)
public interface FMFeignClient {

    @GetMapping("/api/fm/outlets/merchant/{merchantId}")
    DivFmApiResponse<List<DivFmOutletDto>> getOutletsByMerchantId(@PathVariable Integer merchantId);

    @GetMapping("/api/outlets/available-outlets/{areaId}")
    List<DivOutletDto> getOutletsByAreaId(@PathVariable Integer areaId);

    @GetMapping("/api/fm/campaign/all")
    List<DivMealTypeTimingResponseDto> getAllMealTypeTimings();


    @GetMapping("/api/fm/internal/promotion-plans/{promotionPlanId}/schedule-details")
    PromotionScheduleDetailsDto getPromotionScheduleDetails(@PathVariable("promotionPlanId") Integer promotionPlanId);


    @GetMapping("/api/fm/campaign/{mealTypeTimingsId}/exists")
    Boolean existsMealTypeTiming(@PathVariable Integer mealTypeTimingsId);

    @GetMapping("/api/fm/outlets/getOutletDetails")
    DivOutletDetailsDto getOutletDetails(@RequestParam Integer outletId, @RequestParam String userType, @RequestParam(required = false) Integer customerId);

    @GetMapping("/api/fm/products/exists")
    Boolean existsProductInOutlet(@RequestParam Integer outletId, @RequestParam Integer productId);

    @GetMapping("/api/fm/products/active-product-ids")
    List<Integer> getActiveProductIdsByOutlet(
            @RequestParam Integer outletId);
}