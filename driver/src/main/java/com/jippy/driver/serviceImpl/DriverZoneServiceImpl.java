package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.DriverZoneDto;
import com.jippy.driver.entity.DriverZone;
import com.jippy.driver.exception.DriverZoneException;
import com.jippy.driver.mapper.DriverZoneMapper;
import com.jippy.driver.repositary.DriverZoneRepository;
import com.jippy.driver.service.DriverZoneService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DriverZoneServiceImpl implements DriverZoneService {

    private final DriverZoneRepository zoneRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

    @Override
    @Transactional
    public String createZone(DriverZoneDto zoneDto) {

        log.info("API_START | Creating driver zone");

        validateZoneRequest(zoneDto);

        log.info("ZONE_CREATE_REQUEST | zoneName={}",
                zoneDto.getZoneName());

        String zoneName = zoneDto.getZoneName().trim();

        if (zoneRepository.findByZoneName(zoneName).isPresent()) {

            throw new DriverZoneException("Zone with name '" + zoneName + "' already exists!");
        }

        MultiPolygon multiPolygon = convertToJtsPolygon(zoneDto.getBoundary());

        validateGeometry(multiPolygon);

        if (zoneRepository.existsBySpatialBoundary(multiPolygon)) {

            throw new DriverZoneException("A boundary with this exact shape already exists!");
        }

        DriverZone zone = DriverZoneMapper.toEntity(zoneDto, multiPolygon);

        zone.setZoneName(zoneName);
        zone.setCreatedAt(LocalDateTime.now());

        if (zone.getStatus() == null) {
            zone.setStatus("Y");
        }

        DriverZone savedZone = zoneRepository.save(zone);

        log.info("API_SUCCESS | Driver zone created | zoneId={}", savedZone.getZoneId());

        return "Zone: " + savedZone.getZoneName() + " created successfully!";
    }

    @Override
    @Transactional(readOnly = true)
    public DriverZoneDto getZoneById(Integer zoneId) {

        log.info("API_START | Fetching driver zone | zoneId={}", zoneId);

        if (zoneId == null || zoneId <= 0) {
            throw new DriverZoneException("Valid zone ID is required!");
        }

        DriverZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new DriverZoneException(
                        "Zone not found with ID: " + zoneId
                ));

        log.info(
                "ZONE_ENTITY_DATA | zoneId={} | zoneName={} | boundaryNull={} | status={}",
                zone.getZoneId(),
                zone.getZoneName(),
                zone.getBoundary() == null,
                zone.getStatus()
        );

        DriverZoneDto response = DriverZoneMapper.toDto(zone);

        log.info(
                "ZONE_DTO_DATA | zoneId={} | boundaryNull={} | polygonCount={}",
                response.getZoneId(),
                response.getBoundary() == null,
                response.getBoundary() != null
                        ? response.getBoundary().size()
                        : 0
        );

        log.info("API_SUCCESS | Driver zone fetched | zoneId={}", zoneId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverZoneDto> getAllZones() {

        log.info("API_START | Fetching all driver zones");

        List<DriverZone> zones = zoneRepository.findAll();

        List<DriverZoneDto> response = zones.stream().map(DriverZoneMapper::toDto).toList();

        log.info("API_SUCCESS | Driver zones fetched | count={}", response.size());

        return response;
    }

    @Override
    @Transactional
    public String updateZone(Integer zoneId, DriverZoneDto zoneDto) {

        log.info("API_START | Updating driver zone | zoneId={}", zoneId);

        validateZoneRequest(zoneDto);

        log.info("ZONE_UPDATE_REQUEST | zoneName={}",
                zoneDto.getZoneName());

        DriverZone existingZone = zoneRepository.findById(zoneId).orElseThrow(() -> new DriverZoneException("Zone not found with ID: " + zoneId));

        String zoneName = zoneDto.getZoneName().trim();

        zoneRepository.findByZoneName(zoneName).filter(zone -> !zone.getZoneId().equals(zoneId)).ifPresent(zone -> {
            throw new DriverZoneException("Zone with name '" + zoneName + "' already exists!");
        });

        MultiPolygon multiPolygon = convertToJtsPolygon(zoneDto.getBoundary());

        validateGeometry(multiPolygon);

        boolean boundaryExists =
                zoneRepository.existsBySpatialBoundaryExcludingZone(
                        multiPolygon,
                        zoneId
                );

        if (boundaryExists) {

            throw new DriverZoneException(
                    "A boundary with this exact shape already exists!"
            );
        }

        existingZone.setZoneName(zoneName);
        existingZone.setBoundary(multiPolygon);
        existingZone.setUpdatedAt(LocalDateTime.now());
        existingZone.setUpdatedBy(zoneDto.getCreatedBy());

        zoneRepository.save(existingZone);

        log.info("API_SUCCESS | Driver zone updated | zoneId={}", zoneId);

        return "Zone: " + existingZone.getZoneName() + " updated successfully!";
    }


    @Override
    @Transactional
    public String updateZoneStatus(Integer zoneId, String status) {

        log.info("API_START | Updating zone status | zoneId={}, status={}",
                zoneId, status);

        if (zoneId == null) {
            throw new DriverZoneException("Zone ID cannot be null!");
        }

        if (status == null || status.isBlank()) {
            throw new DriverZoneException("Status cannot be empty!");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals("Y")
                && !normalizedStatus.equals("N")) {

            throw new DriverZoneException(
                    "Status must be Y or N!"
            );
        }

        DriverZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new DriverZoneException(
                        "Zone not found with ID: " + zoneId
                ));

        zone.setStatus(normalizedStatus);
        zone.setUpdatedAt(LocalDateTime.now());

        zoneRepository.save(zone);

        log.info("API_SUCCESS | Zone status updated | zoneId={}, status={}",
                zoneId, normalizedStatus);

        return "Zone: " + zone.getZoneName()
                + " status updated successfully!";
    }


    private void validateZoneRequest(DriverZoneDto zoneDto) {

        if (zoneDto == null) {
            throw new DriverZoneException("Zone request cannot be null!");
        }

        if (zoneDto.getZoneName() == null || zoneDto.getZoneName().isBlank()) {

            throw new DriverZoneException("Zone name cannot be empty!");
        }

        if (zoneDto.getBoundary() == null || zoneDto.getBoundary().isEmpty()) {

            throw new DriverZoneException("Zone boundary cannot be empty!");
        }
    }


    private MultiPolygon convertToJtsPolygon(List<List<List<DriverZoneDto.CoordinateDTO>>> boundary) {

        List<Polygon> polygonsList = new ArrayList<>();

        for (List<List<DriverZoneDto.CoordinateDTO>> rawPolygon : boundary) {

            if (rawPolygon == null || rawPolygon.isEmpty()) {

                throw new DriverZoneException("Polygon must contain an exterior ring!");
            }

            List<DriverZoneDto.CoordinateDTO> exteriorRingCoords = rawPolygon.get(0);

            if (exteriorRingCoords == null || exteriorRingCoords.size() < 4) {

                throw new DriverZoneException("Polygon exterior ring must contain at least 4 coordinates!");
            }

            Coordinate[] coordinates = exteriorRingCoords.stream().map(c -> {

                if (c == null) {
                    throw new DriverZoneException("Coordinate cannot be null!");
                }

                return new Coordinate(c.getLongitude(), c.getLatitude());
            }).toArray(Coordinate[]::new);

            if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {

                throw new DriverZoneException("Polygon exterior ring must be closed!");
            }

            LinearRing exteriorRing = geometryFactory.createLinearRing(coordinates);

            Polygon polygon = geometryFactory.createPolygon(exteriorRing, null);

            polygonsList.add(polygon);
        }

        Polygon[] polygonArray = polygonsList.toArray(Polygon[]::new);

        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygonArray);

        multiPolygon.setSRID(4326);

        return multiPolygon;
    }

    private void validateGeometry(MultiPolygon multiPolygon) {

        if (multiPolygon == null || multiPolygon.isEmpty()) {

            throw new DriverZoneException("Zone boundary geometry cannot be empty!");
        }

        if (!multiPolygon.isValid()) {

            throw new DriverZoneException("Zone boundary geometry is invalid!");
        }

        if (multiPolygon.getSRID() != 4326) {

            throw new DriverZoneException("Zone boundary must use SRID 4326!");
        }
    }
}
