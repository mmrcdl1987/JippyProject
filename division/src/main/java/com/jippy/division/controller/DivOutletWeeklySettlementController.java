package com.jippy.division.controller;


import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.enums.DivSettlementFilter;
import com.jippy.division.service.DivOutletWeeklySettlementService;
import com.jippy.division.serviceImpl.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/div")
@RequiredArgsConstructor
@Slf4j
public class DivOutletWeeklySettlementController {

    private final DivOutletWeeklySettlementService divOutletWeeklySettlementService;
    private final EmailService emailService;

    @GetMapping("/getOutletWeeklySettlement")
    @Operation(summary = "Get Outlet Weekly Settlement Details", description = "Retrieves outlet weekly settlement details based on the provided weekly settlement ID. " + "The response includes outlet information, settlement amount, payment status, " + "transaction details, order count, and deductions for the specified settlement record.")
    public ResponseEntity<DivOutletWeeklySettlementResponseDto> getOutletWeeklySettlement(@Parameter(description = "Unique identifier of the weekly settlement record", example = "3") @RequestParam Integer weeklySettlementId) {

        log.info("Received request to fetch outlet weekly settlement details for weeklySettlementId : {}", weeklySettlementId);

        DivOutletWeeklySettlementResponseDto response = divOutletWeeklySettlementService.getOutletWeeklySettlement(weeklySettlementId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get weekly settlement history")
    public ResponseEntity<List<DivOutletWeeklySettlementResponseDto>> getWeeklySettlements(

            @RequestParam Integer merchantId,

            @RequestParam(required = false) Integer outletId,

            @RequestParam DivSettlementFilter filter) {

        log.info("GET_WEEKLY_SETTLEMENT_HISTORY | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        return ResponseEntity.ok(divOutletWeeklySettlementService.getWeeklySettlements(merchantId, outletId, filter));
    }


    //     for gmail integration
//    @PostMapping("/sendOutletSettlementMail")
//    public String sendOutletSettlementMail(@RequestParam Integer weeklySettlementId) {
//
//        divOutletWeeklySettlementService.sendOutletSettlementMail(weeklySettlementId);
//
//        return "OutletSettlement Mail Sent Successfully";
//    }


    //testing mail integration
    @GetMapping("/testMail")
    public String testMail() {

        emailService.sendTestMail();

        return "Mail Sent";
    }
}