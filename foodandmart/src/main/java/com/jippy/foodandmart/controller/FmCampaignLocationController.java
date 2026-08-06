package com.jippy.foodandmart.controller;
import com.jippy.foodandmart.dto.FmCampaignLocationResponse;
import com.jippy.foodandmart.dto.FmMealTypeTimingResponse;
import com.jippy.foodandmart.service.FmCampaignLocationService;
import com.jippy.foodandmart.service.FmMealTypeTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/campaign")
@RequiredArgsConstructor
@Slf4j
public class FmCampaignLocationController {

    private final FmCampaignLocationService campaignLocationService;
    private final FmMealTypeTimingService mealTypeTimingService;
    /**
     * Campaign Location API
     * <p>
     * State Selected
     * -> Cities + State Outlets
     * <p>
     * State + City Selected
     * -> Areas + City Outlets
     * <p>
     * State + City + Area Selected
     * -> Area Outlets
     */
    @GetMapping("/location")
    public ResponseEntity<FmCampaignLocationResponse> getCampaignLocation(

            @RequestParam Integer stateId,

            @RequestParam(required = false) Integer cityId,

            @RequestParam(required = false) Integer areaId) {

        log.info("Campaign Location API Request");

        log.info("stateId={}, cityId={}, areaId={}", stateId, cityId, areaId);

        FmCampaignLocationResponse response = campaignLocationService.getCampaignLocation(stateId, cityId, areaId);

        log.info("Campaign Location API Completed Successfully");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/all")
    public ResponseEntity<List<FmMealTypeTimingResponse>> getAllMealTypeTimings() {

        log.info("Received request to fetch all meal type timings.");

        return ResponseEntity.ok(mealTypeTimingService.getAllMealTypeTimings());
    }


    @GetMapping("/{mealTypeTimingsId}/exists")
    public ResponseEntity<Boolean> existsMealTypeTiming(
            @PathVariable Integer mealTypeTimingsId) {

        return ResponseEntity.ok(
                mealTypeTimingService.existsById(mealTypeTimingsId));
    }
}
