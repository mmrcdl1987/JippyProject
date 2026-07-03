package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmSubscriptionPlanRequestDto;
import com.jippy.foodandmart.dto.FmSubscriptionPlanResponseDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;
import com.jippy.foodandmart.service.IFmSubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class FmSubscriptionPlanController {

    private final IFmSubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public FmApiResponse<SubscriptionPlanResponseDto> saveOrUpdateSubscriptionPlan(
            @Valid @RequestBody FmSubscriptionPlanRequestDto request) {

        log.info(
                "SAVE_OR_UPDATE_SUBSCRIPTION_PLAN_API_START | subscriptionPlanId={}",
                request.getSubscriptionPlanId());

        SubscriptionPlanResponseDto response =
                subscriptionPlanService.saveOrUpdate(request);

        log.info(
                "SAVE_OR_UPDATE_SUBSCRIPTION_PLAN_API_END | subscriptionPlanId={}",
                response.getSubscriptionPlanId());

        return FmApiResponse.success(
                request.getSubscriptionPlanId() == null
                        ? "Subscription plan created successfully"
                        : "Subscription plan updated successfully",
                response);
    }

    @GetMapping("/{subscriptionPlanId}")
    public FmApiResponse<SubscriptionPlanResponseDto> getSubscriptionPlanById(
            @PathVariable Integer subscriptionPlanId) {

        log.info(
                "GET_SUBSCRIPTION_PLAN_BY_ID_API_START | subscriptionPlanId={}",
                subscriptionPlanId);

        SubscriptionPlanResponseDto response =
                subscriptionPlanService.getById(subscriptionPlanId);

        log.info(
                "GET_SUBSCRIPTION_PLAN_BY_ID_API_END | subscriptionPlanId={}",
                subscriptionPlanId);

        return FmApiResponse.success(
                "Subscription plan fetched successfully",
                response);
    }

    @GetMapping
    public FmApiResponse<List<SubscriptionPlanResponseDto>> getAllSubscriptionPlans() {

        log.info("GET_ALL_SUBSCRIPTION_PLANS_API_START");

        List<SubscriptionPlanResponseDto> response =
                subscriptionPlanService.getAll();

        log.info(
                "GET_ALL_SUBSCRIPTION_PLANS_API_END | count={}",
                response.size());

        return FmApiResponse.success(
                "Subscription plans fetched successfully",
                response);
    }

    @DeleteMapping("/{subscriptionPlanId}")
    public FmApiResponse<String> deleteSubscriptionPlan(
            @PathVariable Integer subscriptionPlanId) {

        log.info(
                "DELETE_SUBSCRIPTION_PLAN_API_START | subscriptionPlanId={}",
                subscriptionPlanId);

        subscriptionPlanService.delete(subscriptionPlanId);

        log.info(
                "DELETE_SUBSCRIPTION_PLAN_API_END | subscriptionPlanId={}",
                subscriptionPlanId);

        return FmApiResponse.success(
                "Subscription plan deleted successfully",
                "SUCCESS");
    }
    @GetMapping("/area/{areaId}")
    public ResponseEntity<FmApiResponse<List<FmSubscriptionPlanResponseDto>>> getSubscriptionPlans(
            @PathVariable Integer areaId) {

        log.info("Received API request to fetch subscription plans for Area Id: {}", areaId);

        FmApiResponse<List<FmSubscriptionPlanResponseDto>> response =
                subscriptionPlanService.getSubscriptionPlansByAreaId(areaId);

        log.info("Subscription plan API completed successfully for Area Id: {}", areaId);

        return ResponseEntity.ok(response);
    }
}