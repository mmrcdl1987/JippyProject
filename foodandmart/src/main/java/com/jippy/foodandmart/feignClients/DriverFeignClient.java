package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.DriverWalletUpdateResponseDto;
import com.jippy.foodandmart.dto.FmDriverApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmDriverDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "DRIVER")
public interface DriverFeignClient {

    @PutMapping("/api/driver/updateCODAmountByFleetManager")
    DriverWalletUpdateResponseDto updateCODAmountByFleetManager(
            @RequestParam Integer driverId,
            @RequestParam Integer fleetManagerId);

//     for finding the mail from the driver table in Driver Microservice
    @GetMapping("/api/driver/findByEmail")
    FmDriverDto findByEmail(@RequestParam String email);
//
    @GetMapping("/api/driver/getDriverById/{driverId}")
    FmDriverApprovalResponseDTO getDriverById(@PathVariable Integer driverId);
        /**
         * Calls Driver Service and approves the driver.
         */
        @PutMapping("/api/driver/approve/{driverId}")
        void approveDriver(@PathVariable Integer driverId);

    }
