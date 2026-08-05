package com.jippy.division.feignClients;

import com.jippy.division.config.FeignClientConfig;
import com.jippy.division.dto.DivFmApiResponse;
import com.jippy.division.dto.DivFmOutletDto;
import com.jippy.division.dto.DivMealTypeTimingResponseDto;
import com.jippy.division.dto.DivOutletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "foodandmart", configuration = FeignClientConfig.class)
public interface FMFeignClient {

    @GetMapping("/api/fm/outlets/merchant/{merchantId}")
    DivFmApiResponse<List<DivFmOutletDto>> getOutletsByMerchantId(
            @PathVariable Integer merchantId);

    @GetMapping("/api/outlets/available-outlets/{areaId}")
    List<DivOutletDto> getOutletsByAreaId(@PathVariable Integer areaId);

    @GetMapping("/api/fm/campaign/all")
    List<DivMealTypeTimingResponseDto> getAllMealTypeTimings();
}