package com.jippy.driver.controller;


import com.jippy.driver.dto.*;
import com.jippy.driver.service.DriverWalletService;
import com.jippy.driver.service.DriverWalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@Tag(name = "Driver Wallet API")
public class DriverWalletController {

    private static final Logger logger = LoggerFactory.getLogger(DriverWalletController.class);

    @Autowired
    private DriverWalletService walletservice;

    @Autowired
    private DriverWalletTransactionService walletTransactionService;

    /**
     * F
     * API to deduct COD amount after delivery
     */
    @PostMapping("/insertOrUpdateDriverCodBalance")
    @Operation(summary = "Deduct COD amount from driver wallet after delivery")
    public DriverCodResponseDto updateDriverWallet(@RequestBody DriverCodRequestDto requestDto) {

        logger.info("Received COD request for driverId: {}", requestDto.getDriverId());

        return walletservice.processDriverCod(requestDto);
    }

//     * API for fleet manager to update incentive amount for a driver
//     and activate the driver if the updated COD amount is greater than 0

    @PutMapping("/updateCODAmountByFleetManager")
    @Operation(summary = "Fleet manager updates COD amount for a driver and activates the driver if COD amount > 0", description = "Fleet manager can update COD amount for a driver. If the updated COD amount is greater than 0, the driver will be activated. ," + " [API Path ->/api/driver/updateCODAmountByFleetManager?driverId=Integer&fleetManagerId=Integer]")
    public ResponseEntity<DriverWalletUpdateResponseDto> updateCODAmountByFleetManager(@RequestParam Integer driverId, @RequestParam Integer fleetManagerId) {

        DriverWalletUpdateResponseDto response = walletservice.updateCODAmountByFleetManager(driverId, fleetManagerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getDriverWalletTransactions")
    @Operation(summary = "Get Driver Wallet Transactions",
            description = "Fetch all wallet transaction history for a driver using driverId. "
                    + "The API retrieves the driver's wallet using driverId and returns all associated "
                    + "wallet transactions including orderId, COD amount, transaction type, " +
                    "created/updated details and transaction timestamps. " +
                    "[API Path -> /api/driver/getDriverWalletTransactions?driverId=Integer]")
    public ResponseEntity<List<DriverWalletTransactionResponseDto>> getDriverWalletTransactions(@RequestParam Integer driverId) {

        logger.info("Received request to fetch wallet transactions for driverId : {}", driverId);
        logger.info("Fetching wallet transactions for driverId : {}", driverId);

        List<DriverWalletTransactionResponseDto> response =
                walletTransactionService.getDriverWalletTransactions(driverId);

        logger.info("Successfully fetched {} wallet transactions for driverId : {}", response.size(), driverId);

        return ResponseEntity.ok(response);
    }
}   