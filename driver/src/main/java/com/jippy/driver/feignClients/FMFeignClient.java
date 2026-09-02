package com.jippy.driver.feignClients;


import com.jippy.driver.config.FeignClientConfig;
import com.jippy.driver.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "foodandmart",configuration = FeignClientConfig.class)
public interface FMFeignClient {

    @PostMapping("/api/fm/outlets/saveAddressDetails")
    ResponseEntity<DriverAddressRequestDto> saveAddressDetails(@RequestBody DriverAddressRequestDto fmAddressRequestDto);

    @GetMapping("/api/fm/outlets/getAddressDetails")
    ResponseEntity<DriverAddressRequestDto> getAddressDetails(@RequestParam Integer driverId);

    @GetMapping("/api/fm/outlets/location/{outletId}")
    OutletLocationResponseDto getOutletLocation(@PathVariable("outletId") Integer outletId);

    // Fetch outlet name using outlet id
    @GetMapping("/api/fm/outlets/fetchOutletName")
    String fetchOutletName(@RequestParam("outletId") Integer outletId);

    // --------------------------------------------
    // CALL FM SERVICE for DEACTIVATING DRIVER i.e is_active = Y to N in fm_users table
    // --------------------------------------------
    @PostMapping("/api/fm/users/deactivateDriver")
    String deactivateDriver(@RequestParam("userId") Integer userId);

    @PostMapping("/api/fm/users/createUser")
    ResponseEntity<DriverUserDto> createUser(@RequestBody DriverUserDto dto);

    @GetMapping("/api/fm/users/findByUserIdAndUserType")
    ResponseEntity<DriverUserDto> findByUserIdAndUserType(
            @RequestParam("userId") Integer userId,
            @RequestParam("userType") String userType
    );

    @GetMapping("/api/fm/merchants/fetchByMerchantId")
    ResponseEntity<DriverMerchantDto> getMerchantById(@RequestParam Integer merchantId);

    @PutMapping("/api/fm/merchants/updateMerchantProfilePic")
    ResponseEntity<DriverResponseDto> updateMerchantProfilePic(DriverMerchantDto driverMerchantDto);

    @PostMapping("/api/fm/approval-requests/createApprovalRequest")
    void createApprovalRequest(@RequestBody DriverApprovalRequestDTO requestDTO);


// =====================================================
// LOCATION APIs
// =====================================================

    @GetMapping("/api/fm/location/fetchStates")
    ResponseEntity<List<FMStateDto>> fetchStates();

    @GetMapping("/api/fm/location/fetchCityInState")
    ResponseEntity<List<FMCityDto>> fetchCitiesByState(
            @RequestParam("stateId") Integer stateId
    );

    @GetMapping("/api/fm/location/fetchAreaInCity")
    ResponseEntity<List<FMAreaDto>> fetchAreasByCity(
            @RequestParam("cityId") Integer cityId
    );

    @GetMapping("/api/fm/location/findAreaById")
    ResponseEntity<FMAreaDto> findAreaById(
            @RequestParam("areaId") Integer areaId
    );
}
