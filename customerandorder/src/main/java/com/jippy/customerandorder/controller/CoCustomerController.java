package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.iservice.CoCustomerDeliveryService;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co/customers")
@RequiredArgsConstructor
@Slf4j
public class CoCustomerController {

    private final ICoCustomerService customerService;
    private final CoCustomerDeliveryAddressRepository repository;
    private final CoCustomerDeliveryService customerDeliveryAddressService;

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
    @PutMapping("/updateCustomerProfile")
    public ResponseEntity<CoResponseDto> updateCustomerProfile(@RequestBody CoCustomerRequestDto requestDto) {

        log.info("Update customer profile pic API called {} ", requestDto.getCustomerId());

        String profilePicUrl = customerService.updateCustomerProfile(requestDto);

        log.info("UPDATE_CUSTOMER_API_SUCCESS | customerId {}", requestDto.getCustomerId());

        return ResponseEntity.ok(new CoResponseDto(COConstants.STATUS_200, profilePicUrl));
    }

//    ----------------------------------------------------------------------------------------------
    //    to post customer delivery address with latitude and longitude
    @PostMapping("/saveCustomerDeliveryAddress")
    @Operation(summary = "Create Customer Delivery Address",
            description = "Creates and stores a customer delivery address using the provided customer details and geographic coordinates. " + "The latitude and longitude values are converted into a geographic Point location and persisted in the customer_delivery_addresses table. " + "Mandatory fields: customerId, latitude, longitude, doorNo, buildingName, laneNo, area, city and createdBy. " + "Returns the saved customer delivery address details including the generated customerAddressId.")
    public ResponseEntity<CoCustomerDeliveryAddressResponseDto> createCustomerDeliveryAddress
    (@Valid @RequestBody CoCustomerDeliveryAddressRequestDto requestDto) {

        log.info("CREATE_CUSTOMER_ADDRESS_API_START | customerId={}", requestDto.getCustomerId());

        CoCustomerDeliveryAddressResponseDto responseDto = customerDeliveryAddressService.createCustomerDeliveryAddress(requestDto);

        log.info("CREATE_CUSTOMER_ADDRESS_API_SUCCESS | customerAddressId={}", responseDto.getCustomerAddressId());

        return ResponseEntity.ok(responseDto);

    }

    //    get list of all the delivery addresses of a customer based on the customer id
    @GetMapping("/getCustomerDeliveryAddresses")
    @Operation(summary = "Get Customer Delivery Addresses", description = "Fetches all delivery addresses associated with the given customerId. " + "ex input is customerId=123. The API retrieves the list of delivery addresses for" + " the specified customer from the database and returns them as a list of CoCustomerDeliveryAddressResponseDto objects. " + "Each object in the response contains details about a delivery address, including customerAddressId, customerId," + " latitude, longitude, doorNo, buildingName, laneNo, area, and city. If no addresses are found for the provided customerId, an empty list is returned.")
    public ResponseEntity<List<CoCustomerDeliveryAddressResponseDto>> getCustomerDeliveryAddresses(@RequestParam Integer customerId) {

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_API_START | customerId={}", customerId);
        log.info("Fetching delivery addresses for customerId={}", customerId);

        List<CoCustomerDeliveryAddressResponseDto> responseDto = customerDeliveryAddressService.getCustomerDeliveryAddresses(customerId);

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_API_SUCCESS | customerId={} | addressCount={}", customerId, responseDto.size());

        return ResponseEntity.ok(responseDto);
    }

//    to delete a delivery address based on the customer_address_id
    @DeleteMapping("/deleteCustomerDeliveryAddress")
    @Operation(summary = "Delete Customer Delivery Address",
            description = "Deletes a customer delivery address using the provided customerAddressId.")
    public ResponseEntity<CoResponseDto> deleteCustomerDeliveryAddress
            (@RequestParam Integer customerAddressId) {

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_API_START | customerAddressId={}", customerAddressId);
        log.info("Attempting to delete customer delivery address with customerAddressId={}", customerAddressId);

        customerDeliveryAddressService.deleteCustomerDeliveryAddress(customerAddressId);

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_API_SUCCESS | customerAddressId={}", customerAddressId);

        CoResponseDto customerDeliveryAddressDeletedSuccessfullyDto
                = new CoResponseDto(COConstants.STATUS_200,
                "Customer delivery address deleted successfully");

        return ResponseEntity.ok(customerDeliveryAddressDeletedSuccessfullyDto);

    }
    @GetMapping
    public ResponseEntity<List<CoCustomerListDto>>
    getAllCustomers() {

        log.info("GET_ALL_CUSTOMERS_API_START");

        List<CoCustomerListDto> customers =
                customerService.getAllCustomers();

        log.info(
                "GET_ALL_CUSTOMERS_API_SUCCESS | count={}",
                customers.size()
        );

        return ResponseEntity.ok(customers);
    }
    @GetMapping("/wallet/{customerId}")
    public ResponseEntity<CoCustomerWalletResponseDto> getCustomerWallet(
            @PathVariable Integer customerId) {

        log.info("GET_CUSTOMER_WALLET_API_START | customerId={}", customerId);

        CoCustomerWalletResponseDto response = customerService.getCustomerWallet(customerId);

        log.info("GET_CUSTOMER_WALLET_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/wallet/history/{customerId}")
    public ResponseEntity<List<CoWalletTransactionHistoryDto>> getWalletTransactionHistory(
            @PathVariable Integer customerId) {

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_START | customerId={}", customerId);

        List<CoWalletTransactionHistoryDto> response =
                customerService.getWalletTransactionHistory(customerId);

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(response);
    }

}