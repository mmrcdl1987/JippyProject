package com.jippy.driver.mapper;

import com.jippy.driver.dto.DriverZoneDto;
import com.jippy.driver.entity.DriverZone;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public final class DriverZoneMapper {

    private DriverZoneMapper() {
    }
    // DTO TO ENTITY
    public static DriverZone toEntity(DriverZoneDto dto, MultiPolygon boundary) {

        DriverZone zone = new DriverZone();

        zone.setZoneName(dto.getZoneName());
        zone.setBoundary(boundary);
        zone.setCreatedBy(dto.getCreatedBy());

        // Y = Active, N = Inactive
        zone.setStatus(dto.getStatus());

        return zone;
    }

    // ENTITY TO DTO

    public static DriverZoneDto toDto(DriverZone entity) {

        if (entity == null) {
            return null;
        }

        DriverZoneDto dto = new DriverZoneDto();

        dto.setZoneId(entity.getZoneId());
        dto.setZoneName(entity.getZoneName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());

        dto.setBoundary(convertToCoordinates(entity.getBoundary()));

        return dto;
    }
    // MULTIPOLYGON TO DTO COORDINATES

    private static List<List<List<DriverZoneDto.CoordinateDTO>>> convertToCoordinates(MultiPolygon multiPolygon) {

        List<List<List<DriverZoneDto.CoordinateDTO>>> polygons = new ArrayList<>();

        if (multiPolygon == null || multiPolygon.isEmpty()) {
            return polygons;
        }

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {

            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);

            List<List<DriverZoneDto.CoordinateDTO>> rings = new ArrayList<>();

            // Exterior ring
            rings.add(convertRing(polygon.getExteriorRing().getCoordinates()));

            // Interior rings (holes)
            for (int j = 0; j < polygon.getNumInteriorRing(); j++) {

                rings.add(convertRing(polygon.getInteriorRingN(j).getCoordinates()));
            }

            polygons.add(rings);
        }

        return polygons;
    }

    // JTS RING TO DTO COORDINATES

    private static List<DriverZoneDto.CoordinateDTO> convertRing(Coordinate[] coordinates) {

        List<DriverZoneDto.CoordinateDTO> ring = new ArrayList<>();

        if (coordinates == null) {
            return ring;
        }

        for (Coordinate coordinate : coordinates) {

            DriverZoneDto.CoordinateDTO dto = new DriverZoneDto.CoordinateDTO();

            dto.setLongitude(coordinate.getX());
            dto.setLatitude(coordinate.getY());

            ring.add(dto);
        }

        return ring;
    }
}