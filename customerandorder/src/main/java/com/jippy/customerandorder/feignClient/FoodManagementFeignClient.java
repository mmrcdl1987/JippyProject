package com.jippy.customerandorder.feignClient;

import com.jippy.customerandorder.dto.FmNearbyOutletResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "foodandmart",
        contextId = "foodManagementFeignClient"
)
public interface FoodManagementFeignClient {

    @GetMapping(
            "/api/outlets/specialized-outlets/area"
    )
    FmNearbyOutletResponseDto
    fetchSpecializedOutletsByAreaId(
            @RequestParam Integer areaId
    );
}