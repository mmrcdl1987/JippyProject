package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.CustomerResponseDto;
import com.jippy.foodandmart.dto.DriverResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "CUSTOMERANDORDER")
public interface CustomerAndOrderFeignClient {
    @GetMapping("/api/co/driver/getDriverDetails")
    DriverResponseDto getDriverDetails(@RequestParam Integer driverId);

    @GetMapping("/api/co/customers/{customerId}")
    CustomerResponseDto getCustomer(@PathVariable Integer customerId);

//    to fetch frequent orders for a customer, we can fetch the order history for that customer
//    and then calculate the frequency of orders for each outlet. Based on the frequency,
//    we can return the list of outletIds that are most frequently ordered from by that customer.
//    from FM microservice
    @GetMapping("/api/co/orders/frequent")
    List<Integer> getFrequentOutlets(@RequestParam Integer customerId);

    @GetMapping("/api/co/orders/recent")
    Integer getRecentOutlet(@RequestParam Integer customerId);
}
