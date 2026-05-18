package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.CustomerResponseDto;
import com.jippy.foodandmart.dto.DriverResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "CUSTOMERANDORDER")
public interface CustomerAndOrderFeignClient {
    @GetMapping("/api/co/driver/getDriverDetails")
    DriverResponseDto getDriverDetails(@RequestParam Integer driverId);

    @GetMapping("/api/co/customers/{customerId}")
    CustomerResponseDto getCustomer(@PathVariable Integer customerId);
}
