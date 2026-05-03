package com.jippy.customerandorder.controller;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.dto.CoPlaceOrderResponseDto;
import com.jippy.customerandorder.iservice.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class CoOrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<CoPlaceOrderResponseDto> placeOrder(
            @Valid @RequestBody CoPlaceOrderRequestDto placeOrderRequestDto) {

        log.info("API hit: place order | customerId={}, outletId={}",
                placeOrderRequestDto.getCustomerId(), placeOrderRequestDto.getOutletId());

        CoPlaceOrderResponseDto response = orderService.placeOrder(placeOrderRequestDto);

        log.info("Order placed successfully | customerId={}, outletId={}, orderId={}",
                placeOrderRequestDto.getCustomerId(), placeOrderRequestDto.getOutletId(), response.getOrderId());

        return ResponseEntity.ok(response);
    }

}
