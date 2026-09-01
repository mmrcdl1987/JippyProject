package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoCartReminderDto;
import com.jippy.customerandorder.dto.CoCartResponseDto;
import com.jippy.customerandorder.dto.CoCartUpdateRequestDto;
import com.jippy.customerandorder.iservice.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/co/cart")
@RequiredArgsConstructor
@Slf4j
public class COCartController {

    private final ICartService cartService;

    @PostMapping("/update")
    public ResponseEntity<String> saveOrUpdateCart(@Valid @RequestBody CoCartUpdateRequestDto request) {

        log.info("API_START | SAVE_OR_UPDATE_CART | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={}", request.getCustomerId(), request.getOutletId(), request.getProductId(), request.getVariantOptionId(), request.getQuantity());

        String response = cartService.saveOrUpdateCart(request);

        log.info("API_END | SAVE_OR_UPDATE_CART | customerId={} | productId={} | variantOptionId={} | response={}", request.getCustomerId(), request.getProductId(), request.getVariantOptionId(), response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CoCartResponseDto> getCart(@PathVariable Integer customerId) {

        log.info("API_START | GET_CART | customerId={}", customerId);

        CoCartResponseDto response = cartService.getCart(customerId);

        log.info("API_END | GET_CART | customerId={} | itemCount={} | grandTotal={}", customerId, response.getItems().size(), response.getGrandTotal());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/reminders")
    public ResponseEntity<List<CoCartReminderDto>> getCartReminderCustomers() {

        log.info("API_START | GET_CART_REMINDERS");

        List<CoCartReminderDto> response = cartService.getCartReminderCustomers();

        log.info("API_END | GET_CART_REMINDERS | totalCustomers={}", response.size());

        return ResponseEntity.ok(response);
    }
}