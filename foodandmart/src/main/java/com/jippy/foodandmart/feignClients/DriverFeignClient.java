package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.DriverWalletUpdateResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "DRIVER")
public interface DriverFeignClient {

    @PutMapping("/api/driver/updateCODAmountByFleetManager")
    DriverWalletUpdateResponseDto updateCODAmountByFleetManager(
            @RequestParam Integer driverId,
            @RequestParam Integer fleetManagerId);
}