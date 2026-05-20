package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverZoneAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverZoneAssignmentRepository extends JpaRepository<DriverZoneAssignment, Integer> {

    // Check duplicate assignment of driver and zone and return true if exists
//    this prevents assigning the same zone to the same driver multiple times
    boolean existsByDriverDriverIdAndZoneZoneId(Integer driverId, Integer zoneId);
}