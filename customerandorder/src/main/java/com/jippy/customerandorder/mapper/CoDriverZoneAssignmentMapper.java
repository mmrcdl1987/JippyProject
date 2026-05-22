/*
package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoDriverZoneAssignmentResponseDto;
import com.jippy.customerandorder.entity.CoDriverZoneAssignment;

public class CoDriverZoneAssignmentMapper {

    // Convert entity into response dto
    public static CoDriverZoneAssignmentResponseDto mapToResponseDto(CoDriverZoneAssignment assignment) {

        CoDriverZoneAssignmentResponseDto responseDto = new CoDriverZoneAssignmentResponseDto();

        responseDto.setDriverZoneAssignmentId(assignment.getDriverZoneAssignmentId());

//        get driver id and zone id from assignment and set in response dto
        responseDto.setDriverId(assignment.getDriver().getDriverId());

        responseDto.setZoneId(assignment.getZone().getZoneId());

        responseDto.setMessage("Zone assigned successfully");

        return responseDto;
    }
}*/
