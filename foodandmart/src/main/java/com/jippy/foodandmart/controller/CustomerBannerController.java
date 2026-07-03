package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.AreaBannerCacheDto;
import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.service.CustomerBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/banners")
@RequiredArgsConstructor
@Slf4j
public class CustomerBannerController {

    private final CustomerBannerService customerBannerService;

    @GetMapping("/{areaId}")
    public FmApiResponse<AreaBannerCacheDto> getBanners(
            @PathVariable Integer areaId) {

        log.info("GET_CUSTOMER_BANNERS | areaId={}", areaId);

        AreaBannerCacheDto response =
                customerBannerService.getCustomerBanners(areaId);

        return FmApiResponse.success(
                "Customer banners fetched successfully",
                response);
    }
}