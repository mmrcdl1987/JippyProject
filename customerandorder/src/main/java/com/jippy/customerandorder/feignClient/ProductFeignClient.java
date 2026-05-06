package com.jippy.customerandorder.feignClient;

import com.jippy.customerandorder.dto.FmProductDetailResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "foodandmart")
public interface ProductFeignClient {

    @GetMapping("/api/pricing/{productId}")
    FmProductDetailResponseDto getProductById(
            @PathVariable("productId") Integer productId
    );
}