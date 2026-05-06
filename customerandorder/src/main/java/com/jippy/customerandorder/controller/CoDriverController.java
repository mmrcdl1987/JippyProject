package com.jippy.customerandorder.controller;


import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.iservice.ICoDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/co/driver")
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
}