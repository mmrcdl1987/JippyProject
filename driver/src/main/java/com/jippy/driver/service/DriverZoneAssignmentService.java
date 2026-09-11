package com.jippy.driver.service;


import com.jippy.driver.dto.DriverZoneAssignmentRequestDto;
import com.jippy.driver.dto.DriverZoneAssignmentResponseDto;
import com.jippy.driver.dto.ZoneStatusToggleRequestDto;

public interface DriverZoneAssignmentService {

    // Assign zone to driver
    DriverZoneAssignmentResponseDto assignZoneToDriver(DriverZoneAssignmentRequestDto requestDto);

    String statusToggleForZone(ZoneStatusToggleRequestDto requestDTO);
}