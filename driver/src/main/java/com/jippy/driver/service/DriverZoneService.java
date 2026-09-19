package com.jippy.driver.service;

import com.jippy.driver.dto.DriverZoneDto;
import com.jippy.driver.entity.DriverZone;

import java.util.List;
import java.util.Optional;

public interface DriverZoneService {

    Optional<DriverZone> findActiveZoneByCoordinates(
            Double latitude,
            Double longitude
    );

    String createZone(DriverZoneDto zoneDto);

    DriverZoneDto getZoneById(Integer zoneId);

    List<DriverZoneDto> getAllZones();

    String updateZone(
            Integer zoneId,
            DriverZoneDto zoneDto
    );

    String updateZoneStatus(
            Integer zoneId,
            String status
    );
}