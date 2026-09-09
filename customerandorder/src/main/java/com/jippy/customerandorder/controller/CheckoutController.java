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
@RequestMapping("/api/co/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final ICheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CoCheckoutResponseDto> checkout(@Valid @RequestBody CoCheckoutRequestDto requestDto) {

        log.info("CHECKOUT_API_START | customerId={} | outletId={} | customerAddressId={}", requestDto.getCustomerId(), requestDto.getOutletId(), requestDto.getCustomerAddressId());

        CoCheckoutResponseDto response = checkoutService.checkout(requestDto);

        log.info("CHECKOUT_API_SUCCESS | customerId={} | outletId={} | itemCount={} | itemTotal={} | toPay={}", requestDto.getCustomerId(), response.getOutletId(), response.getItems() != null ? response.getItems().size() : 0, response.getItemTotal(), response.getToPay());

        return ResponseEntity.ok(response);
    }
}