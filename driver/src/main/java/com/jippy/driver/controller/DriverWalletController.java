package com.jippy.driver.controller;


import com.jippy.driver.dto.DriverCodRequestDto;
import com.jippy.driver.dto.DriverCodResponseDto;
import com.jippy.driver.service.DriverWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
@Tag(name = "Driver Wallet API")
public class DriverWalletController {

    private static final Logger logger = LoggerFactory.getLogger(DriverWalletController.class);

    @Autowired
    private DriverWalletService service;

    /**
     * API to deduct COD amount after delivery
     */
    @PostMapping("/insertOrUpdateDriverCodBalance")
    @Operation(summary = "Deduct COD amount from driver wallet after delivery")
    public DriverCodResponseDto updateDriverWallet(
            @RequestBody DriverCodRequestDto requestDto) {

        logger.info("Received COD request for driverId: {}", requestDto.getDriverId());

        return service.processDriverCod(requestDto);
    }
}