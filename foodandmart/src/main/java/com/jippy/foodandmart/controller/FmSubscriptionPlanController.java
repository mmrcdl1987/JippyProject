package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmSubscriptionPlanDto;
import com.jippy.foodandmart.service.IFmSubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fm/subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Plans API", description = "Create and Update Subscription Plans")
public class FmSubscriptionPlanController {

    private final IFmSubscriptionPlanService service;

    @PostMapping("/createAndUpdateSubscriptionPlans")
    @Operation(summary = "Create or Update Subscription Plan", description = "Creates new plan if id is null, updates existing plan if id is present holds in service logic ")
    public ResponseEntity<FmSubscriptionPlanDto> createAndUpdatePlans
            (@Valid @RequestBody FmSubscriptionPlanDto dto) {

        log.info("Create/Update subscription plan API called");

        FmSubscriptionPlanDto updatePlans = service.createAndUpdatePlans(dto);
        return ResponseEntity.ok(updatePlans);
    }
}