package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoCartResponseDto;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;

import com.jippy.customerandorder.iservice.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class COCartController {

    private final ICartService cartService;

    @PostMapping("/update")
    public ResponseEntity<String> updateCart(
            @Valid @RequestBody CoCartUpdateRequestDto request) {

        log.info("Cart update request received | customerId={}, productId={}, quantity={}",
                request.getCustomerId(), request.getProductId(), request.getQuantity());

        String response = cartService.updateCart(request);

        log.info("Cart update completed successfully | customerId={}, response={}", 
                request.getCustomerId(), response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CoCartResponseDto> getCart(
            @PathVariable Integer customerId) {

        log.info("API hit: get cart | customerId={}", customerId);

        CoCartResponseDto response = cartService.getCart(customerId);

        log.info("Cart fetched successfully | customerId={}, items={}, grandTotal={}", 
                customerId, response.getItems().size(), response.getGrandTotal());

        return ResponseEntity.ok(response);
    }
}