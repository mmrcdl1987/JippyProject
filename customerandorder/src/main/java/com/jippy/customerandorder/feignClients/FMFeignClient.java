package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.config.FeignClientConfig;
import com.jippy.customerandorder.dto.*;
//import com.jippy.foodandmart.entity.FmUser;
//import com.jippy.foodandmart.dto.LoginRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "foodandmart",configuration = FeignClientConfig.class)
public interface FMFeignClient {

    /*@PostMapping("/api/fm/outlets/saveAddressDetails")
    ResponseEntity<CoAddressRequestDto> saveAddressDetails(@RequestBody CoAddressRequestDto fmAddressRequestDto);

    @GetMapping("/api/fm/outlets/getAddressDetails")
    ResponseEntity<CoAddressRequestDto> getAddressDetails(@RequestParam Integer driverId);*/

    @GetMapping("/api/fm/pricing/{productId}")
    FmProductDetailResponseDto getProductById(@PathVariable("productId") Integer productId);

    @GetMapping("/api/fm/outlets/location/{outletId}")
    OutletLocationResponseDto getOutletLocation(@PathVariable("outletId") Integer outletId);

    @GetMapping("/api/fm/outlets/specialized-outlets/area")
    FmNearbyOutletResponseDto fetchSpecializedOutletsByAreaId(@RequestParam Integer areaId);

    // Fetch outlet name using outlet id
    @GetMapping("/api/fm/outlets/fetchOutletName")
    String fetchOutletName(@RequestParam Integer outletId);

    // --------------------------------------------------------------------------------
    // CALL FM SERVICE for DEACTIVATING DRIVER i.e is_active = Y to N in fm_users table
    // --------------------------------------------------------------------------------

    @PostMapping("/api/fm/users/deactivateDriver")
    String deactivateDriver(@RequestParam("userId") Integer userId);

   /* @PostMapping("/api/fm/users/createUser")
    ResponseEntity<FmUser> createUser(@RequestBody CoUserDto dto);*/
   @GetMapping("/api/fm/outlets/specialized-outlets/nearby")
   CoNearbyOutletResponseDto
   fetchNearbySpecializedOutlets(
           @RequestParam("latitude") Double latitude,
           @RequestParam("longitude") Double longitude);
    //---------------------------------------------------------------------------------------------------
    //    this is from "Fm Merchant Settlement Controller" From Fm microservice to fetch product details,
    //    outlet details and area name using their respective id's
    /*
     Fetch product details using product id
     */
    @GetMapping("/api/fm/product")
    CoFmProductDto getSettlementProductById(@RequestParam Integer productId);

    /*
     Fetch outlet details using outlet id
     */
    @GetMapping("/api/fm/settlement/outlet")
    CoFmOutletDto getOutletById(@RequestParam("outletId") Integer outletId);

    /**
     *
     * Get all outlets for merchant
     */
    @GetMapping("/api/fm/outlets/merchant/{merchantId}")
    CoFmApiResponse<List<CoFmOutletDto>> getOutletsByMerchantId(@PathVariable Integer merchantId);


    @GetMapping("/api/fm/pricing/{productId}/outlet/{outletId}")
    FmProductDetailResponseDto getProductByIdAndOutletId(
            @PathVariable("productId") Integer productId,
            @PathVariable("outletId") Integer outletId);

    @PostMapping("/api/fm/users/createUser")
    ResponseEntity<CoUserDto> createUser(@RequestBody CoUserDto dto);

    @GetMapping("/api/fm/users/findByUserIdAndUserType")
    ResponseEntity<CoUserDto> findByUserIdAndUserType(@RequestParam Integer userId,
            @RequestParam String userType);

    @PostMapping(path = "/api/fm/auth/login")
    public ResponseEntity<?> login(@RequestBody CoLoginDto loginDto);

    @GetMapping("/api/fm/areas")
    List<FmAreaDto> getAllAreas();

    @GetMapping("/api/fm/meal-reminder/current-meal-type")
    CoCurrentMealTypeResponse getCurrentMealType();

    @GetMapping("/api/fm/location/findAreaById")
    public String findAreaById(@RequestParam Integer areaId);

}
