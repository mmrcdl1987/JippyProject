package com.jippy.division.controller;

import com.jippy.division.dto.DivRefundDetailsDto;
import com.jippy.division.service.DivOrderRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/div/payment/refund")
@AllArgsConstructor
@Slf4j
public class DivOrderRefundController {

    private final DivOrderRefundService orderRefundService;

    @PostMapping("/orderRefund")
    public ResponseEntity<String> orderRefund(@RequestParam String orderId, @RequestParam String reason) {
        try {
            log.info("Processing refund for orderId: {}, reason: {}", orderId, reason);

            String response = orderRefundService.orderRefund(orderId, reason);
            return ResponseEntity.status(HttpStatus.OK).body("Refund request processed successfully. Refund ID: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing refund: " + e.getMessage());
        }
    }

    //    =======================================================================================
//    =======================================================================================
    @GetMapping("/getRefundDetails")
    @Operation(summary = "Get refund details by order ID", description = """
            Fetches refund transaction details using
            application order ID.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Refund details fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Refund details not found")})
    public ResponseEntity<DivRefundDetailsDto> getRefundDetails
    (@RequestParam String orderId) {

        log.info("GET getRefundDetails request received. orderId={}", orderId);

        return ResponseEntity.ok(orderRefundService.getRefundDetails(orderId));
    }
}
