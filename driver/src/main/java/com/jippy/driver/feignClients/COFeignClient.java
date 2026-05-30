package com.jippy.driver.feignClients;

import com.jippy.driver.config.FeignClientConfig;
import com.jippy.driver.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@FeignClient(name = "customerandorder", contextId = "coFeignClient", configuration = FeignClientConfig.class)
public interface COFeignClient {

    @GetMapping("/api/co/customers/address/location")
    DriveCustomerLocationDto getCustomerLocation(@RequestParam Integer customerAddressId);

    @GetMapping("/api/co/fetchEarnings")
    DriverEarningsDto fetchDriverEarnings(@RequestParam Integer driverId,

                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @PutMapping("/api/co/orders/{orderId}/deliver")
    String deliverOrder(@PathVariable String orderId, @RequestParam Integer driverId);

    @GetMapping("/api/co/orders/price-breakup")
    DriveOrderPriceBreakupDto getOrderPriceBreakup(@RequestParam String orderId);

    @GetMapping("/api/co/orders")
    DriveOrderDto getOrder(@RequestParam String orderId);


    @GetMapping("/api/co/order-rejections/driver/rejected-orders/count")
    Long fetchRejectedOrdersCount(@RequestParam Integer driverId);

    @GetMapping("/api/co/customers/{customerId}")
    ResponseEntity<DriverCustomerResponseDto> getCustomer(@PathVariable Integer customerId);

    @PutMapping("/api/co/customers/updateCustomerProfilePic")
    ResponseEntity<DriverResponseDto> updateCustomerProfilePic(DriverCustomerResponseDto driverCustomerResponseDto);

}