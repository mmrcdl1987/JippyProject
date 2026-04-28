package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class FmPricingController {

    private final IPricingService pricingService;

    private static final Logger log = LoggerFactory.getLogger(FmPricingController.class);

    // GET OUTLETS =================
    @GetMapping("/outlets")
    public ResponseEntity<List<FmOutletDto>> getOutlets(
            @RequestParam Integer areaId,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String search) {

        log.info("API START: GET /outlets | areaId={} | isApproved={} | search={}",
                areaId, isApproved, search);

        List<FmOutletDto> response =
                pricingService.getOutlets(areaId, isApproved, search);

        log.info("API END: GET /outlets | count={}", response.size());

        return ResponseEntity.ok(response);
    }

    // GET PRODUCTS
    @GetMapping("/products")
    public ResponseEntity<List<FmProductResponseDto>> getProducts(
            @RequestParam List<Integer> outletIds,
            @RequestParam boolean isApproved) {

        log.info("API START: GET /products | outletIds={} | isApproved={}",
                outletIds, isApproved);

        List<FmProductResponseDto> response =
                pricingService.getProducts(outletIds, isApproved);

        log.info("API END: GET /products | count={}", response.size());

        return ResponseEntity.ok(response);
    }

    //  UPDATE PRICES
    @PostMapping("/update")
    public ResponseEntity<FmResponseDto> updatePrices(
            @Valid @RequestBody FmPriceUpdateRequestDto dto,
            @RequestParam boolean isApproved) {

        log.info("API START: POST /update | outletIds={} | itemCount={} | isApproved={}",
                dto.getOutletIds(), dto.getItems().size(), isApproved);

        pricingService.updatePrices(dto, isApproved);

        log.info("API END: POST /update | SUCCESS");

        return ResponseEntity.ok(
                new FmResponseDto(
                        FmAppConstants.STATUS_200,
                        FmAppConstants.MSG_PRICE_UPDATED
                )
        );
    }

    //  BULK UPDATE
    @PostMapping("/bulk-update")
    public ResponseEntity<FmResponseDto> bulkUpdatePrices(
            @Valid @RequestBody FmBulkPriceUpdateRequestDto dto,
            @RequestParam boolean isApproved) {
        log.info("API START: BULK UPDATE | outlets={} | priceModel={} | value={} | isApproved={}",
                dto.getOutletIds(), dto.getPriceModel(), dto.getValue(), isApproved);

        pricingService.bulkUpdatePrices(dto, isApproved);

        log.info("API END: BULK UPDATE SUCCESS");

        return ResponseEntity.ok(
                new FmResponseDto(
                        FmAppConstants.STATUS_200,
                        FmAppConstants.MSG_PRICE_UPDATED
                )
        );
    }
}