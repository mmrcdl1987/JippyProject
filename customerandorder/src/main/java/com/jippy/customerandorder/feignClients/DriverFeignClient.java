package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.dto.DeliveryChargeCalculationRequestDto;
import com.jippy.customerandorder.dto.DeliveryChargeCalculationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "driver")
public interface DriverFeignClient {

    @PostMapping("/api/driver/delivery-charge/calculate")
    DeliveryChargeCalculationResponseDto calculateDeliveryCharge(@RequestBody DeliveryChargeCalculationRequestDto requestDto);
}