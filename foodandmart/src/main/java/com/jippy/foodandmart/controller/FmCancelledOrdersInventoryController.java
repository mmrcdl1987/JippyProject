package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmAssignCancelledOrderRequestDto;
import com.jippy.foodandmart.dto.FmAssignCancelledOrderResponseDto;
import com.jippy.foodandmart.service.FmCancelledOrdersInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/cancelled-orders")
@RequiredArgsConstructor
public class FmCancelledOrdersInventoryController {

    private final FmCancelledOrdersInventoryService service;

    @PostMapping("/assign")
    public ResponseEntity<FmAssignCancelledOrderResponseDto> assignCancelledOrder(@RequestBody FmAssignCancelledOrderRequestDto requestDto) {

        return ResponseEntity.ok(service.assignCancelledOrder(requestDto));
    }
}