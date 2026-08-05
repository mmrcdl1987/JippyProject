package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.config.FeignClientConfig;
import com.jippy.customerandorder.dto.DeliveryChargeCalculationRequestDto;
import com.jippy.customerandorder.dto.DeliveryChargeCalculationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "driver",configuration = FeignClientConfig.class)
public interface DriverFeignClient {

    @PostMapping("/api/driver/delivery-charge/calculate")
    DeliveryChargeCalculationResponseDto calculateDeliveryCharge(@RequestBody DeliveryChargeCalculationRequestDto requestDto);

//    @GetMapping("/api/driver/getZonesByType")
//    ResponseEntity<List<CoZoneResponseDto>> getZonesByType(@RequestParam(value = "zoneType") String zoneType);
//
//    @GetMapping("/api/driver/findCommunityById")
//     ResponseEntity<CoZoneResponseDto> findCommunityById(@RequestParam(value = "communityId") Integer communityId);
//
//    @GetMapping("/api/driver/findCustomerInCommunity")
//     ResponseEntity<Integer> findCustomerInCommunity(@RequestParam Double latitude,
//            @RequestParam Double longitude);
//
//    @GetMapping("/api/driver/checkCustomerAddressWithCommunity")
//    public ResponseEntity<Integer> checkCustomerAddressWithCommunity(@RequestParam Double latitude,
//            @RequestParam Double longitude,@RequestParam Integer communityId);
}