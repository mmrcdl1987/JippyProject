package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.dto.WelcomeCouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "division")
public interface DivisionFeignClient {

    @GetMapping("/api/div/coupons/welcome")
    ResponseEntity<List<WelcomeCouponDto>> getActiveWelcomeCoupons();

}