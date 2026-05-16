package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoCheckoutRequestDto;
import com.jippy.customerandorder.dto.CoCheckoutResponseDto;
import com.jippy.customerandorder.iservice.ICheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final ICheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CoCheckoutResponseDto> checkout(@Valid @RequestBody CoCheckoutRequestDto requestDto) {

        log.info("CHECKOUT API START | customerId={}, outletId={}", requestDto.getCustomerId(), requestDto.getOutletId());

        CoCheckoutResponseDto response = checkoutService.checkout(requestDto);

        log.info("CHECKOUT API SUCCESS | customerId={}, toPay={}", requestDto.getCustomerId(), response.getToPay());

        return ResponseEntity.ok(response);
    }
}