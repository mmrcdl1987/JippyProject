package com.jippy.customerandorder.controller;
import com.jippy.customerandorder.dto.CoOrderCheckoutTaxRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutTaxResponseDto;
import com.jippy.customerandorder.iservice.CoOrderCheckoutTaxService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/co/order-checkout-tax")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CoOrderCheckoutTaxController {

    private final CoOrderCheckoutTaxService service;

    @PostMapping
    public ResponseEntity<CoOrderCheckoutTaxResponseDto> create(@Valid @RequestBody CoOrderCheckoutTaxRequestDto request) {

        log.info("CONTROLLER_START | CREATE_ORDER_CHECKOUT_TAX");

        CoOrderCheckoutTaxResponseDto response = service.create(request);

        log.info("CONTROLLER_SUCCESS | CREATE_ORDER_CHECKOUT_TAX | id={}", response.getOrderCheckoutTaxId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CoOrderCheckoutTaxResponseDto>> getAll() {

        log.info("CONTROLLER_START | GET_ALL_ORDER_CHECKOUT_TAX");

        List<CoOrderCheckoutTaxResponseDto> response = service.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderCheckoutTaxId}")
    public ResponseEntity<CoOrderCheckoutTaxResponseDto> getById(@PathVariable @NotNull(message = "Order checkout tax id is required") Integer orderCheckoutTaxId) {

        log.info("CONTROLLER_START | GET_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        CoOrderCheckoutTaxResponseDto response = service.getById(orderCheckoutTaxId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderCheckoutTaxId}")
    public ResponseEntity<CoOrderCheckoutTaxResponseDto> update(@PathVariable @NotNull(message = "Order checkout tax id is required") Integer orderCheckoutTaxId,

                                                                @Valid @RequestBody CoOrderCheckoutTaxRequestDto request) {

        log.info("CONTROLLER_START | UPDATE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        CoOrderCheckoutTaxResponseDto response = service.update(orderCheckoutTaxId, request);

        log.info("CONTROLLER_SUCCESS | UPDATE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderCheckoutTaxId}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull(message = "Order checkout tax id is required") Integer orderCheckoutTaxId) {

        log.info("CONTROLLER_START | DELETE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        service.delete(orderCheckoutTaxId);

        log.info("CONTROLLER_SUCCESS | DELETE_ORDER_CHECKOUT_TAX | id={}", orderCheckoutTaxId);

        return ResponseEntity.noContent().build();
    }
}