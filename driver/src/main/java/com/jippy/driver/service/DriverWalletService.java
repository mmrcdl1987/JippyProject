package com.jippy.driver.service;


import com.jippy.driver.dto.DriverCodRequestDto;
import com.jippy.driver.dto.DriverCodResponseDto;
import com.jippy.driver.dto.DriverWalletUpdateResponseDto;
import org.springframework.web.bind.annotation.RequestParam;

public interface DriverWalletService {
    DriverCodResponseDto processDriverCod(DriverCodRequestDto dto);

//    for fleet manager to update COD amount for driver and
//    activate the driver if the updated COD amount is greater than 0
    DriverWalletUpdateResponseDto updateCODAmountByFleetManager(Integer driverId,Integer fleetManagerId);

}
