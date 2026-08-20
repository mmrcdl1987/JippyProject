package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoSaveWalletPointsRequestDTO;
import com.jippy.customerandorder.dto.CoSaveWalletPointsResponseDTO;

import com.jippy.customerandorder.iservice.CoWalletPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Wallet", description = "Customer wallet point management APIs")
public class CoWalletPointsController {

    private final CoWalletPointsService walletPointsService;

    @PostMapping("/saveWalletPointsEqualToOrderAmountDiscounted")
    @Operation(summary = "Save wallet points for delivered order", description = "Adds order_amount_discounted to customer " + "wallet balance_points and saves " + "transaction history.")
    @ApiResponses({

            @ApiResponse(responseCode = "200", description = "Wallet points saved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CoSaveWalletPointsResponseDTO.class), examples = @ExampleObject(value = """
                    {
                      "orderId": "ORD012",
                      "customerId": 112,
                      "walletId": 14,
                      "orderAmountDiscounted": 400.00,
                      "previousBalancePoints": 2500.00,
                      "updatedBalancePoints": 2900.00,
                      "transactionPoints": 2900.00,
                      "pointsType": "ORDER_VALUE_POINTS",
                      "status": "SUCCESS",
                      "message": "Wallet points saved successfully"
                    }
                    """))),

            @ApiResponse(responseCode = "400", description = "Validation failed"),

            @ApiResponse(responseCode = "404", description = "Order not found")})
    public ResponseEntity<CoSaveWalletPointsResponseDTO> saveWalletPointsEqualToOrderTotalAmount(@Valid @RequestBody CoSaveWalletPointsRequestDTO requestDTO) {

        log.info("SAVE_WALLET_POINTS_API | orderId={}", requestDTO.getOrderId());

        return ResponseEntity.ok(walletPointsService.saveWalletPointsEqualToOrderAmountDiscounted(requestDTO));
    }
}