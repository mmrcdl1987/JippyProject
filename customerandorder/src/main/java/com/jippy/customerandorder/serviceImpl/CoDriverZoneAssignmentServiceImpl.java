package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoDriverZoneAssignmentRequestDto;
import com.jippy.customerandorder.dto.CoDriverZoneAssignmentResponseDto;
import com.jippy.customerandorder.entity.CoDriver;
import com.jippy.customerandorder.entity.CoDriverZoneAssignment;
import com.jippy.customerandorder.entity.CoZone;
import com.jippy.customerandorder.iservice.CoDriverZoneAssignmentService;
import com.jippy.customerandorder.mapper.CoDriverZoneAssignmentMapper;
import com.jippy.customerandorder.repository.CoDriverRepository;
import com.jippy.customerandorder.repository.CoDriverZoneAssignmentRepository;
import com.jippy.customerandorder.repository.CoZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoDriverZoneAssignmentServiceImpl implements CoDriverZoneAssignmentService {

    //        from CoZone Table--> we will find the zone using latitude and longitude and then we will assign
//        that zone to driver in CoDriverZoneAssignment table
    private final CoZoneRepository zoneRepository;

    //    from CoDriver Table--> we will find the driver using driver id and then we will assign
//    the zone to that driver in CoDriverZoneAssignment table
    private final CoDriverRepository driverRepository;

    //    from CoDriverZoneAssignment Table--> we will save the assignment of zone to driver and also
//    we will check if the driver is already assigned to that zone or not
    private final CoDriverZoneAssignmentRepository assignmentRepository;

    @Override
    public CoDriverZoneAssignmentResponseDto assignZoneToDriver(CoDriverZoneAssignmentRequestDto requestDto) {

        log.info("Zone assignment started for driver id : {}", requestDto.getDriverId());

        // Validate driver existence id driver exist or not
        CoDriver driver = driverRepository.findById(requestDto.getDriverId()).orElseThrow(() -> {

            log.error("Driver not found with id : {}", requestDto.getDriverId());

            return new ResourceNotFoundException("Driver not found with" + requestDto.getDriverId());
        });

        // Find zone using coordinates in CoZone table
        CoZone foundZone = new CoZone();
//        if zone id is not provided in request dto then we will find the zone using
//        latitude and longitude otherwise we will find the zone using zone id
        if(!(requestDto.getZoneId() != null)) {
             foundZone = zoneRepository.findZoneByCoordinates(requestDto.getLatitude(), requestDto.getLongitude());

            // Throw exception if no zone found
            if (foundZone == null) {

                log.error("No zone found for latitude : {} and longitude : {}", requestDto.getLatitude(), requestDto.getLongitude());

                throw new ResourceNotFoundException("No zone found for given coordinates" + "Latitude: " + requestDto.getLatitude() + "Longitude: " + requestDto.getLongitude() + " try other zones or check the coordinates");
            }

//        to assign zone id to driver we need to check if the driver is already assigned to that zone or not
//        if already assigned then we will throw exception otherwise we will assign the zone to driver
            log.info("Zone found with zone id : {}", foundZone.getZoneId());
        } else{
           foundZone = zoneRepository.findById(requestDto.getZoneId()).orElseThrow(() -> {

                log.error("Zone not found with id : {}", requestDto.getZoneId());

                return new ResourceNotFoundException("Zone not found with id" + requestDto.getZoneId());
            });
        }

        // Prevent duplicate assignment of the same zone to the same driver
//        this checks if there is already an assignment for the given driver
//        and zone in the CoDriverZoneAssignment table
        boolean alreadyAssigned = assignmentRepository.existsByDriverDriverIdAndZoneZoneId(driver.getDriverId(), foundZone.getZoneId());

        if (alreadyAssigned) {

            log.error("Driver already assigned to zone id : {}", foundZone.getZoneId());

            throw new ResourceNotFoundException("Driver already assigned to this zone");
        }

        // Create assignment entity and set values
        CoDriverZoneAssignment assignment = new CoDriverZoneAssignment();

//       Set driver and zone in assignment entity
        assignment.setDriver(driver);

//     Set the found zone to the assignment entity, which will be saved in the CoDriverZoneAssignment
//     table to establish the relationship between the driver and the assigned zone
        assignment.setZone(foundZone);


        assignment.setCreatedBy(1);

        assignment.setUpdatedBy(1);

        assignment.setUpdatedAt(LocalDateTime.now());

        // Save assignment in CoDriverZoneAssignment table
        assignment = assignmentRepository.save(assignment);

        log.info("Zone assigned successfully for driver id : {}", driver.getDriverId());

        // Return response dto with assignment details
        return CoDriverZoneAssignmentMapper.mapToResponseDto(assignment);
    }
}