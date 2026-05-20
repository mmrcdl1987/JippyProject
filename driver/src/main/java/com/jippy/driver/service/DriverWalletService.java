package com.jippy.driver.service;


import com.jippy.driver.dto.DriverCodRequestDto;
import com.jippy.driver.dto.DriverCodResponseDto;

public interface DriverWalletService {
    DriverCodResponseDto processDriverCod(DriverCodRequestDto dto);

}
