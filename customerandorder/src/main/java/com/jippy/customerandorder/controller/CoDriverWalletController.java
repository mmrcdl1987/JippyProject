/*
package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoDriverCodRequestDto;
import com.jippy.customerandorder.dto.CoDriverCodResponseDto;
import com.jippy.customerandorder.iservice.CoDriverWalletService;
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
@RequestMapping("/api/co")
@Tag(name = "Driver Wallet API")
public class CoDriverWalletController {

    private static final Logger logger = LoggerFactory.getLogger(CoDriverWalletController.class);

    @Autowired
    private CoDriverWalletService service;

    */
/**
     * API to deduct COD amount after delivery
     *//*

    @PostMapping("/insertOrUpdateDriverCodBalance")
    @Operation(summary = "Deduct COD amount from driver wallet after delivery")
    public CoDriverCodResponseDto updateDriverWallet(
            @RequestBody CoDriverCodRequestDto requestDto) {

        logger.info("Received COD request for driverId: {}", requestDto.getDriverId());

        return service.processDriverCod(requestDto);
    }
}*/
