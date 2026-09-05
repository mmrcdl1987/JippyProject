package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import com.jippy.customerandorder.dto.CoPaymentRequest;
import com.jippy.customerandorder.entity.CoPaymentModes;
import com.jippy.customerandorder.iservice.IOrderSettingsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/co/order-settings")
@RequiredArgsConstructor
@Slf4j
public class CoOrderSettingsController {

    private final IOrderSettingsService orderSettingsService;

    @PostMapping
    public ResponseEntity<CoOrderSettingsResponseDto> saveOrUpdate(@Valid @RequestBody CoOrderSettingsRequestDto requestDto) {

        log.info("SAVE OR UPDATE ORDER SETTINGS API START");

        CoOrderSettingsResponseDto response = orderSettingsService.saveOrUpdate(requestDto);

        log.info("SAVE OR UPDATE ORDER SETTINGS API SUCCESS");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getPaymentModeById")
    public ResponseEntity<CoPaymentModeResponse> getPaymentModeById(@RequestParam Integer paymentModeId) {

       log.info("GET PAYMENT MODE BY ID API START");

        CoPaymentModeResponse response = orderSettingsService.getPaymentModeById(paymentModeId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getActivePaymentModes")
    public ResponseEntity<List<CoPaymentModeResponse>> getActivePaymentModes() {

        log.info("GET ACTIVE PAYMENT MODEs API START");
        List<CoPaymentModeResponse> response = orderSettingsService.getActivePaymentModes();
        return ResponseEntity.ok(response);
    }

    /*
     * CREATE
     *
     * POST /api/payment-modes
     */
    @PostMapping("/payment-mode")
    public ResponseEntity<CoPaymentModeResponse> create(@Valid @RequestBody CoPaymentRequest request) {

        /*
         * Replace this with your authenticated
         * logged-in user ID.
         */
        Integer userId = 1;
        CoPaymentModeResponse response = orderSettingsService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * UPDATE
     *
     * PUT /api/payment-modes/{id}
     */
    @PutMapping("/payment-mode/{id}")
    public ResponseEntity<CoPaymentModeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CoPaymentRequest request) {

        Integer userId = 1;
        return ResponseEntity.ok(orderSettingsService.update(id, request, userId));
    }

    /*
     * SOFT DELETE
     *
     * DELETE /api/payment-modes/{id}
     */
    @DeleteMapping("/payment-mode/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {

        Integer userId = 1;
        orderSettingsService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payment-mode")
    public ResponseEntity<List<CoPaymentModeResponse>> getAllPaymentModes() {
        return ResponseEntity.ok(orderSettingsService.getAllPaymentModes());
    }

}