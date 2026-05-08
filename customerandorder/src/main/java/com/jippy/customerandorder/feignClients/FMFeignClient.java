package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.dto.CoAddressRequestDto;
import com.jippy.customerandorder.dto.FmProductDetailResponseDto;
import com.jippy.customerandorder.dto.OutletLocationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="foodandmart")
public interface FMFeignClient {

    @PostMapping("/api/outlets/saveAddressDetails")
    public ResponseEntity<CoAddressRequestDto> saveAddressDetails(@RequestBody CoAddressRequestDto fmAddressRequestDto);

    @GetMapping("/api/outlets/getAddressDetails")
    public ResponseEntity<CoAddressRequestDto>  getAddressDetails(@RequestParam Integer driverId) ;

    @GetMapping("/api/pricing/{productId}")
    FmProductDetailResponseDto getProductById(@PathVariable("productId") Integer productId);

    @GetMapping("/api/outlets/location/{outletId}")
    OutletLocationResponseDto getOutletLocation(@PathVariable("outletId") Integer outletId);

}
