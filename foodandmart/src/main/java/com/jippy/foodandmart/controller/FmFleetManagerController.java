package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmUpdateCODResponseDto;
import com.jippy.foodandmart.service.FmFleetManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
public class FmFleetManagerController {

    private final FmFleetManagerService fmFleetManagerService;

    @PutMapping("/updateCODAmountByFleetManager")
    public ResponseEntity<FmUpdateCODResponseDto> updateCODAmountByFleetManager(@RequestParam Integer driverId) {

        return ResponseEntity.ok(fmFleetManagerService.updateCODAmountByFleetManager(driverId));
    }
}