package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoSalesReportResponseDto;
import com.jippy.customerandorder.enums.CoSalesReportFilter;
import com.jippy.customerandorder.iservice.CoSalesReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co")
@RequiredArgsConstructor
@Slf4j
public class CoSalesReportController {

    private final CoSalesReportService coSalesReportService;

    @Operation(
            summary = "Sales Report",
            description = "Fetch merchant/outlet sales report with ALL, LAST_WEEK and LAST_MONTH filters")
    @GetMapping("/sales-report")
    public ResponseEntity<CoSalesReportResponseDto> getSalesReport(@RequestParam Integer merchantId,

                                                                   @RequestParam(required = false) Integer outletId,

                                                                   @RequestParam CoSalesReportFilter filter) {

        log.info("API_START | SALES_REPORT | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        CoSalesReportResponseDto response = coSalesReportService.getSalesReport(merchantId, outletId, filter);

        log.info("API_END | SALES_REPORT | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        return ResponseEntity.ok(response);
    }
}