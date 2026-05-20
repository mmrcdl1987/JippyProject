package com.jippy.driver.mapper;


import com.jippy.driver.dto.DriverZoneAssignmentResponseDto;
import com.jippy.driver.entity.DriverZoneAssignment;

public class DriverZoneAssignmentMapper {

    // Convert entity into response dto
    public static DriverZoneAssignmentResponseDto mapToResponseDto(DriverZoneAssignment assignment) {

        DriverZoneAssignmentResponseDto responseDto = new DriverZoneAssignmentResponseDto();

        responseDto.setDriverZoneAssignmentId(assignment.getDriverZoneAssignmentId());

//        get driver id and zone id from assignment and set in response dto
        responseDto.setDriverId(assignment.getDriver().getDriverId());

        responseDto.setZoneId(assignment.getZone().getZoneId());

        responseDto.setMessage("Zone assigned successfully");

        return responseDto;
    }
}