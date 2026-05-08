package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoDriverZoneAssignmentRequestDto;
import com.jippy.customerandorder.dto.CoDriverZoneAssignmentResponseDto;

public interface CoDriverZoneAssignmentService {

    // Assign zone to driver
    CoDriverZoneAssignmentResponseDto assignZoneToDriver(CoDriverZoneAssignmentRequestDto requestDto);
}