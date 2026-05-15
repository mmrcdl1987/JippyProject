package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.FeignClientConfig;
import com.jippy.customerandorder.dto.CoAddressRequestDto;
import com.jippy.customerandorder.dto.FmNearbyOutletResponseDto;
import com.jippy.customerandorder.dto.FmProductDetailResponseDto;
import com.jippy.customerandorder.dto.OutletLocationResponseDto;
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
    
}
