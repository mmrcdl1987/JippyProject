package com.jippy.customerandorder.controller;


import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.iservice.ICoDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co/driver")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver API", description = "Driver and KYC operations")
public class CoDriverController {

    private final ICoDriverService driverService;

    //    post driver details ,driver kyc from this this(Co Microservice) and address Details from (FM microservices)
    @PostMapping("/postDriverDetails")
    @Operation(summary = "Create Driver", description = "Creates driver, KYC, and address")
    public ResponseEntity<CoDriverDto> postDriverDetails(@Valid @RequestBody CoDriverDto dto) {

        log.info("POST API called that created driver:");

        return ResponseEntity.ok(driverService.postDriverDetails(dto));
    }

    //    get driver details ,driver kyc from this this(Co Microservice) and address Details from (FM microservices)
    @GetMapping("/getDriverDetails")
    @Operation(summary = "Get Driver", description = "Fetch driver by ID")
    public ResponseEntity<CoDriverDto> getDriverDetails(@RequestParam Integer driverId) {

        log.info("GET API called with id to get all details of driver : {}", driverId);

        return ResponseEntity.ok(driverService.getDriverDetails(driverId));
    }

    //    update driver details ,driver kyc from this this(Co Microservice)
//    and address Details from (FM microservices)
    @PutMapping("/updateDriverDetails")
    @Operation(summary = "Update Driver Details", description = "Updates editable driver and address fields")
    public ResponseEntity<CoDriverDto> updateDriverDetails(@RequestParam Integer driverId, @Valid @RequestBody CoDriverDto dto) {

        log.info("Updating driver with id: {}", driverId);

        return ResponseEntity.ok(driverService.updateDriverDetails(driverId, dto));
    }

    @PostMapping("/createZones")
    @Operation(summary = "Create Zones", description = "Create Zones")
    public ResponseEntity<CoResponseDto> createZones(@Valid @RequestBody CoZoneDto zoneDto) {

        log.info("POST API called for created zones:");
        String message = driverService.createZones(zoneDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CoResponseDto(COConstants.STATUS_201, message));
    }

    @GetMapping("/fetchEarnings")
    @Operation(summary = "Fetch Driver Earnings", description = "Fetch total earnings and orders count for a driver on a particular date")
    public ResponseEntity<CoDriverEarningsDto> fetchEarnings(@RequestParam Integer driverId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("date format must be [YYYY-MM-dd] for date: {}", date);
        log.info("Fetch earnings API called for driver id: {}", driverId);

        return ResponseEntity.ok(driverService.fetchEarnings(driverId, date));
    }

    //    for api fetchOrderEarningsHistory to just fetch
//    outlet name based on outlet id which is mapped to driver id to
//    use CoDriverController microservice
    @GetMapping("/fetchOrderEarningsHistory")
    @Operation(summary = "Fetch Order Earnings History", description = "Fetch complete order earnings history of driver")
    public ResponseEntity<List<CoDriverOrderHistoryDto>> fetchOrderEarningsHistory(@RequestParam Integer driverId) {

        log.info("Fetch order earnings history API called for driver id: {}", driverId);

        return ResponseEntity.ok(driverService.fetchOrderEarningsHistory(driverId));
    }

//    to fetch total earnings details of driver like total pick up charges,
//    total delivery charges, total tips, total surge fee and total earnings
//    which is sum of all these and also count of rejected orders for that driver
    @GetMapping("/fetchTotalEarnings")
    @Operation(summary = "Fetch Total Earnings", description = "Fetch total earnings details of driver")
    public ResponseEntity<CoDriverTotalEarningsDto> fetchTotalEarnings(@RequestParam Integer driverId) {

        log.info("Fetch total earnings API called for driver id: {}", driverId);

        return ResponseEntity.ok(driverService.fetchTotalEarnings(driverId));
    }

}