package com.jippy.driver.service;

import com.jippy.driver.entity.DriverZone;

import java.util.Optional;

public interface DriverZoneService {

    Optional<DriverZone> findActiveZoneByCoordinates(
            Double latitude,
            Double longitude
    );
}