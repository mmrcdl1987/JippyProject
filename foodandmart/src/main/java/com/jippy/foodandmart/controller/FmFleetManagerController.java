package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmUpdateCODResponseDto;
import com.jippy.foodandmart.service.FmFleetManagerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
public class FmFleetManagerController {

    private final FmFleetManagerService fmFleetManagerService;

    @PutMapping("/updateCODAmountByFleetManager")
    @Operation(summary = "Fleet manager updates COD amount for a driver and activates the driver if COD amount > 0",
            description = "Fleet manager can update COD amount for a driver. If the updated COD amount is greater than 0, the driver will be activated. ," +
                    " [API Path ->/api/fm/updateCODAmountByFleetManager?driverId=Integer&fleetManagerId=Integer]")
    public ResponseEntity<FmUpdateCODResponseDto> updateCODAmountByFleetManager(@RequestParam Integer driverId,
                                                                              @RequestParam Integer fleetManagerId) {

        log.info("Received request to update COD amount for driverId: {} by fleet manager", driverId);
        log.info("Calling service to update COD amount for driverId: {}", driverId);
        return ResponseEntity.ok(fmFleetManagerService.updateCODAmountByFleetManager(driverId, fleetManagerId));
    }
}