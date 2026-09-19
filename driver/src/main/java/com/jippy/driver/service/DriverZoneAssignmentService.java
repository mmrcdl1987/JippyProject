package com.jippy.driver.service;


import com.jippy.driver.dto.DriverZoneAssignmentRequestDto;
import com.jippy.driver.dto.DriverZoneAssignmentResponseDto;
import com.jippy.driver.dto.ZoneStatusToggleRequestDto;
import com.jippy.driver.entity.DriverZone;
import com.jippy.driver.exception.DriverBadRequestException;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface DriverZoneAssignmentService {

    // Assign zone to driver
    DriverZoneAssignmentResponseDto assignZoneToDriver(DriverZoneAssignmentRequestDto requestDto);

    String statusToggleForZone(ZoneStatusToggleRequestDto requestDTO);


}