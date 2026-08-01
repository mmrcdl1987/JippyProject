package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmCurrentMealTypeResponse;
import com.jippy.foodandmart.service.IFmMealReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/meal-reminder")
@RequiredArgsConstructor
@Slf4j
public class FmMealReminderController {

    private final IFmMealReminderService mealReminderService;

    @GetMapping("/current-meal-type")
    public ResponseEntity<FmCurrentMealTypeResponse> getCurrentMealType() {

        log.info("API_START | GET_CURRENT_MEAL_TYPE");

        FmCurrentMealTypeResponse response =
                mealReminderService.getCurrentMealType();

        log.info("API_END | GET_CURRENT_MEAL_TYPE");

        return ResponseEntity.ok(response);
    }
}