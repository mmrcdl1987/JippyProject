package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.OutletSubscriptionResponseDto;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OutletSubscriptionPlanController {

    private final OutletSubscriptionPlanService outletSubscriptionPlanService;

    @GetMapping("/outlets/{outletId}/subscription")
    @Operation(summary = "Get Outlet Subscription Details")
    public ResponseEntity<OutletSubscriptionResponseDto> getOutletSubscription(@PathVariable @Min(value = 1, message = "Outlet Id must be greater than 0") Integer outletId) {

        log.info("API_START | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId);

        try {

            OutletSubscriptionResponseDto response = outletSubscriptionPlanService.getOutletSubscription(outletId);

            log.info("API_SUCCESS | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            log.error("API_ERROR | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId, ex);

            throw ex;

        } finally {

            log.info("API_END | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId);
        }
    }
}