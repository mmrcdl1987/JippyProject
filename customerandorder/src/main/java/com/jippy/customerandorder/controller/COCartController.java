package com.jippy.customerandorder.controller;

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

        log.info("Cart update completed | response={}", response);

        return ResponseEntity.ok(response);
    }
}