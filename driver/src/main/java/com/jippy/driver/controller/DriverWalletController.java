package com.jippy.driver.controller;


import com.jippy.driver.dto.DriverCodRequestDto;
import com.jippy.driver.dto.DriverCodResponseDto;
import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverWalletUpdateResponseDto;
import com.jippy.driver.service.DriverWalletService;
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
    private DriverWalletService service;

    /**F
     * API to deduct COD amount after delivery
     */
    @PostMapping("/insertOrUpdateDriverCodBalance")
    @Operation(summary = "Deduct COD amount from driver wallet after delivery")
    public DriverCodResponseDto updateDriverWallet(
            @RequestBody DriverCodRequestDto requestDto) {

        logger.info("Received COD request for driverId: {}", requestDto.getDriverId());

        return service.processDriverCod(requestDto);
    }

//     * API for fleet manager to update incentive amount for a driver
//     and activate the driver if the updated COD amount is greater than 0

    @PutMapping("/updateCODAmountByFleetManager")
    @Operation(summary = "Fleet manager updates COD amount for a driver and activates the driver if COD amount > 0",
            description = "Fleet manager can update COD amount for a driver. If the updated COD amount is greater than 0, the driver will be activated. ," +
            " [API Path ->/api/driver/updateCODAmountByFleetManager?driverId=Integer&fleetManagerId=Integer]")
    public ResponseEntity<DriverWalletUpdateResponseDto>
    updateCODAmountByFleetManager(
            @RequestParam Integer driverId ,
            @RequestParam Integer fleetManagerId) {

        DriverWalletUpdateResponseDto response = service.updateCODAmountByFleetManager(driverId,fleetManagerId);

        return ResponseEntity.ok(response);
    }

}   