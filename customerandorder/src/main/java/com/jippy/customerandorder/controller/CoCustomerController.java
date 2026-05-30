package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/co/customers")
@RequiredArgsConstructor
@Slf4j
public class CoCustomerController {

    private final ICoCustomerService customerService;
    private final CoCustomerDeliveryAddressRepository repository;

    // CREATE CUSTOMER
    @PostMapping
    public CoCustomer createCustomer(@RequestBody CoCustomerRequestDto dto) {

        log.info("Customer create request received: {}", dto);

        return customerService.createCustomer(dto);
    }

    // CONVERT POINTS
    @PostMapping("/convert-points/{customerId}")
    public CoWalletResponseDto convertPoints(@PathVariable Integer customerId) {

        log.info("Convert points request received for customerId : {}", customerId);

        return customerService.convertPoints(customerId);
    }
    @PostMapping("/daily-streak/{customerId}")
    public CoCustomerStreakResponseDto updateDailyStreak(

            @PathVariable Integer customerId,

            @RequestParam(required = false) LocalDate date) {

        log.info("Daily streak request received");

        return customerService.updateDailyStreak(customerId, date);
    }

    @PostMapping("/wallet/transfer")
    public CoWalletTransferResponseDto transferWalletPoints(@RequestBody CoWalletTransferRequestDto requestDto) {

        log.info("Wallet transfer request received");

        return customerService.transferWalletPoints(requestDto);
    }

    // GET CUSTOMER

    @GetMapping("/{customerId}")
    public ResponseEntity<CoCustomerResponseDto> getCustomer(@PathVariable Integer customerId) {

        log.info("GET_CUSTOMER_API_START | customerId={}", customerId);

        CoCustomerResponseDto customer = customerService.getCustomer(customerId);

        log.info("GET_CUSTOMER_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(customer);
    }

    // UPDATE CUSTOMER
    @PutMapping("/{customerId}")
    public ResponseEntity<CoResponseDto> updateCustomer(@PathVariable Integer customerId, @Valid @RequestBody CoCustomerRequestDto requestDto) {

        log.info("UPDATE_CUSTOMER_API_START | customerId={} | email={} | phone={}", customerId, requestDto.getEmail(), requestDto.getPhoneNumber());

        customerService.updateCustomer(customerId, requestDto);

        log.info("UPDATE_CUSTOMER_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(new CoResponseDto(COConstants.STATUS_200, COConstants.MSG_SUCCESS));
    }

    // GET CUSTOMER LOCATION
    @GetMapping("/address/location")
    public CoCustomerLocationDto getCustomerLocation(@RequestParam Integer customerAddressId) {

        log.info("GET_CUSTOMER_LOCATION_API_START | customerAddressId={}", customerAddressId);

        CustomerLocationProjection projection = repository.getCustomerLocation(customerAddressId);

        CoCustomerLocationDto dto = new CoCustomerLocationDto();

        dto.setLatitude(projection.getLatitude());
        dto.setLongitude(projection.getLongitude());

        log.info("GET_CUSTOMER_LOCATION_API_SUCCESS | customerAddressId={}", customerAddressId);

        return dto;
    }

    // Update Customer Profile Pic
    @PutMapping("/updateCustomerProfilePic")
    public ResponseEntity<CoResponseDto> updateCustomerProfilePic(@RequestBody CoCustomerRequestDto requestDto) {

        log.info("Update customer profile pic API called {} ", requestDto.getCustomerId());

        String profilePicUrl = customerService.updateCustomerProfilePic(requestDto);

        log.info("UPDATE_CUSTOMER_API_SUCCESS | customerId {}", requestDto.getCustomerId());

        return ResponseEntity.ok(new CoResponseDto(COConstants.STATUS_200, profilePicUrl));
    }


}