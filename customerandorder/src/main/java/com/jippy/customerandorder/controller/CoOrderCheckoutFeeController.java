package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderCheckoutFeeRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutFeeResponseDto;
import com.jippy.customerandorder.iservice.CoOrderCheckoutFeeService;
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
@RequestMapping("/api/co/order-checkout-fee")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CoOrderCheckoutFeeController {

    private final CoOrderCheckoutFeeService service;

    @PostMapping
    public ResponseEntity<CoOrderCheckoutFeeResponseDto> create(@Valid @RequestBody CoOrderCheckoutFeeRequestDto request) {

        log.info("CONTROLLER_START | CREATE_ORDER_CHECKOUT_FEE");

        CoOrderCheckoutFeeResponseDto response = service.create(request);

        log.info("CONTROLLER_SUCCESS | CREATE_ORDER_CHECKOUT_FEE | id={}", response.getOrderCheckoutFeeId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CoOrderCheckoutFeeResponseDto>> getAll() {

        log.info("CONTROLLER_START | GET_ALL_ORDER_CHECKOUT_FEE");

        List<CoOrderCheckoutFeeResponseDto> response = service.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderCheckoutFeeId}")
    public ResponseEntity<CoOrderCheckoutFeeResponseDto> getById(@PathVariable @NotNull(message = "Order checkout fee id is required") Integer orderCheckoutFeeId) {

        log.info("CONTROLLER_START | GET_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        CoOrderCheckoutFeeResponseDto response = service.getById(orderCheckoutFeeId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderCheckoutFeeId}")
    public ResponseEntity<CoOrderCheckoutFeeResponseDto> update(@PathVariable @NotNull(message = "Order checkout fee id is required") Integer orderCheckoutFeeId,

                                                                @Valid @RequestBody CoOrderCheckoutFeeRequestDto request) {

        log.info("CONTROLLER_START | UPDATE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        CoOrderCheckoutFeeResponseDto response = service.update(orderCheckoutFeeId, request);

        log.info("CONTROLLER_SUCCESS | UPDATE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderCheckoutFeeId}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull(message = "Order checkout fee id is required") Integer orderCheckoutFeeId) {

        log.info("CONTROLLER_START | DELETE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        service.delete(orderCheckoutFeeId);

        log.info("CONTROLLER_SUCCESS | DELETE_ORDER_CHECKOUT_FEE | id={}", orderCheckoutFeeId);

        return ResponseEntity.noContent().build();
    }
}