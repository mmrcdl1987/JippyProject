package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.config.FeignClientConfig;
import com.jippy.customerandorder.dto.*;
import com.jippy.foodandmart.entity.FmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "foodandmart",configuration = FeignClientConfig.class)
public interface FMFeignClient {

    @PostMapping("/api/fm/outlets/saveAddressDetails")
    ResponseEntity<CoAddressRequestDto> saveAddressDetails(@RequestBody CoAddressRequestDto fmAddressRequestDto);

    @GetMapping("/api/fm/outlets/getAddressDetails")
    ResponseEntity<CoAddressRequestDto> getAddressDetails(@RequestParam Integer driverId);

    @GetMapping("/api/fm/pricing/{productId}")
    FmProductDetailResponseDto getProductById(@PathVariable("productId") Integer productId);

    @GetMapping("/api/fm/outlets/location/{outletId}")
    OutletLocationResponseDto getOutletLocation(@PathVariable("outletId") Integer outletId);

    @GetMapping("/api/fm/outlets/specialized-outlets/area")
    FmNearbyOutletResponseDto fetchSpecializedOutletsByAreaId(@RequestParam Integer areaId);

    // Fetch outlet name using outlet id
    @GetMapping("/api/fm/outlets/fetchOutletName")
    String fetchOutletName(@RequestParam Integer outletId);

    // --------------------------------------------
    // CALL FM SERVICE for DEACTIVATING DRIVER i.e is_active = Y to N in fm_users table
    // --------------------------------------------
    @PostMapping("/api/fm/users/deactivateDriver")
    String deactivateDriver(@RequestParam("userId") Integer userId);

    @PostMapping("/api/fm/users/createUser")
    ResponseEntity<FmUser> createUser(@RequestBody CoUserDto dto);
    
}
