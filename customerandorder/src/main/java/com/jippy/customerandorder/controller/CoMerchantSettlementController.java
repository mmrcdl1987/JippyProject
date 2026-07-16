package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoMerchantSettlementOutletDto;
import com.jippy.customerandorder.dto.CoMerchantSettlementRequestDto;
import com.jippy.customerandorder.iservice.CoMerchantSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co/merchant-settlement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchant Settlement APIs", description = "APIs for fetching merchant settlement details. Date format : yyyy-MM-dd")
public class CoMerchantSettlementController {

    private final CoMerchantSettlementService coMerchantSettlementService;

    /*
     Fetch merchant settlement details
     between start date and end date
     */
    @Operation(summary = "Fetch merchant settlement details between dates")
    @GetMapping("/getProductDetailsForMerchantSettlement")
    public List<CoMerchantSettlementOutletDto> getProductDetailsForMerchantSettlement(

            @RequestParam("startDate") LocalDate startDate,@RequestParam("endDate") LocalDate endDate) {

        log.info("Received merchant settlement request from {} to {}",startDate, endDate);

        log.info("Date format validated as yyyy-MM-dd");

        return coMerchantSettlementService.getProductDetailsForMerchantSettlement(startDate,endDate);
    }
}