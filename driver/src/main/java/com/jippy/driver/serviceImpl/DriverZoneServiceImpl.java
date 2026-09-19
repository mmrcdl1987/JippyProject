package com.jippy.driver.serviceImpl;

import com.jippy.driver.entity.DriverZone;
import com.jippy.driver.repositary.DriverZoneRepository;
import com.jippy.driver.service.DriverZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverZoneServiceImpl implements DriverZoneService {

    private final DriverZoneRepository zoneRepository;

    @Override
    public Optional<DriverZone> findActiveZoneByCoordinates(Double latitude, Double longitude) {

        log.info("FIND_ACTIVE_ZONE_BY_COORDINATES | latitude={} | longitude={}", latitude, longitude);

        if (latitude == null || longitude == null) {
            log.warn("ZONE_LOOKUP_FAILED | COORDINATES_NULL | latitude={} | longitude={}", latitude, longitude);

            return Optional.empty();
        }

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {

            log.warn("ZONE_LOOKUP_FAILED | INVALID_COORDINATES | latitude={} | longitude={}", latitude, longitude);

            return Optional.empty();
        }

        Optional<DriverZone> zone = zoneRepository.findActiveZoneByCoordinates(latitude, longitude);

        if (zone.isPresent()) {
            log.info("ACTIVE_ZONE_FOUND | zoneId={} | zoneName={}", zone.get().getZoneId(), zone.get().getZoneName());
        } else {
            log.warn("ACTIVE_ZONE_NOT_FOUND | latitude={} | longitude={}", latitude, longitude);
        }

        return zone;
    }
}