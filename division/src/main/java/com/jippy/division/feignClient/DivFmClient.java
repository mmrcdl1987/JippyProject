package com.jippy.division.feignClient;

import com.jippy.division.dto.DivOutletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "foodandmart")
public interface DivFmClient {

    @GetMapping("/api/outlets/available-outlets/{areaId}")
    List<DivOutletDto> getOutletsByAreaId(@PathVariable Integer areaId);
}