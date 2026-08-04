package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmOutletsResponseDto;
import com.jippy.foodandmart.dto.FmProductResponseDto;
import com.jippy.foodandmart.service.FmMerchantSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FM Merchant Settlement APIs")
public class FmMerchantSettlementController {

    private final FmMerchantSettlementService fmMerchantSettlementService;

    /*
     Fetch product details using product id
     */
    @Operation(summary = "Get Product By Id")
    @GetMapping("/product")
    public FmProductResponseDto getSettlementProductById(

            @RequestParam Integer productId) {

        log.info("Received request to fetch product details for product id : {}", productId);

        return fmMerchantSettlementService.getProductById(productId);
    }

    /*
     Fetch outlet details using outlet id
     */
    @Operation(summary = "Get Outlet By Id")
    @GetMapping("/settlement/outlet")
    public FmOutletsResponseDto getOutletById(

            @RequestParam Integer outletId) {
        log.info("******** HIT SETTLEMENT OUTLET API ********");
        log.info("Settlement outletId={}", outletId);


        log.info("Received request to fetch outlet details for outlet id : {}", outletId);

        return fmMerchantSettlementService.getOutletById(outletId);
    }


}