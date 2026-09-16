package com.jippy.foodandmart.controller;
import com.jippy.foodandmart.service.FmAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/address")
@RequiredArgsConstructor
public class FmAddressController {

    private final FmAddressService fmAddressService;


    // GET DRIVER IDs BY AREA
    // Used by Driver Service for Admin Driver Area Filter

    @GetMapping("/driverIdsByArea")
    public ResponseEntity<List<Integer>> getDriverIdsByArea(
            @RequestParam("areaId") Integer areaId
    ) {

        List<Integer> driverIds =
                fmAddressService.getDriverIdsByAreaId(areaId);

        return ResponseEntity.ok(driverIds);
    }

}
