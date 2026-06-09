package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoReorderRequestDto;
import com.jippy.customerandorder.dto.CoReorderResponseDto;
import com.jippy.customerandorder.iservice.ICoReorderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reorder", description = "Reorder completed orders")
public class CoReorderController {

    private final ICoReorderService reorderService;

    @PostMapping("/reorder")
    @Operation(summary = "Reorder Previous Order", description = """
            Rebuild customer cart from a completed/delivered order.
            
            Flow:
            Order History
                ↓
            Reorder
                ↓
            Rebuild Cart
                ↓
            Customer Reviews Cart
                ↓
            Checkout
                ↓
            Place Order
            """)
    public ResponseEntity<CoReorderResponseDto> reorder(@Valid @RequestBody CoReorderRequestDto requestDto) {

        log.info("API_START | REORDER_ORDER | customerId={} | orderId={}", requestDto.getCustomerId(), requestDto.getOrderId());

        try {

            CoReorderResponseDto response = reorderService.reorder(requestDto);

            log.info("API_SUCCESS | REORDER_ORDER | customerId={} | orderId={} | addedItems={} | unavailableItems={}", requestDto.getCustomerId(), requestDto.getOrderId(), response.getAddedItemsCount(), response.getUnavailableItemsCount());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            log.error("API_ERROR | REORDER_ORDER | customerId={} | orderId={} | error={}", requestDto.getCustomerId(), requestDto.getOrderId(), ex.getMessage(), ex);

            throw ex;
        } finally {

            log.info("API_END | REORDER_ORDER | customerId={} | orderId={}", requestDto.getCustomerId(), requestDto.getOrderId());
        }
    }
}