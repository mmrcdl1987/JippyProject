package com.jippy.division.feignclients;

import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.dto.DivPaymentModesDto;
import com.jippy.division.dto.DivPlaceOrderRequestDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "customerandorder")
public interface CoFeignClient {

    @PostMapping("/api/co/placeOrder")
    public ResponseEntity<DivOrderDto> placeOrder(@Valid @RequestBody DivPlaceOrderRequestDto placeOrderRequestDto);

    @GetMapping("/api/co/orders")
    public DivOrderDto getOrder(@RequestParam String orderId);

    @PutMapping("/api/co/updateOrderStatus")
    void updateOrderStatus(DivOrderDto orderDto);

    @GetMapping("/api/co/order-settings/getPaymentModeById")
    public ResponseEntity<DivPaymentModesDto> getPaymentModeById(@RequestParam Integer paymentModeId);
}