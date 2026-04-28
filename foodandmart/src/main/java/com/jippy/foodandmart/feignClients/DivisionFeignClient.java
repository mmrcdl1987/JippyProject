package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.FmDivPriceModelDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "division")
public interface DivisionFeignClient {

   @GetMapping("/api/coupons/getPriceModels")
    public ResponseEntity<List<FmDivPriceModelDto>> getPriceModels();
}
