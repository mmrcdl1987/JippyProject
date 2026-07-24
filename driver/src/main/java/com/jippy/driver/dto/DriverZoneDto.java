package com.jippy.driver.dto;

import lombok.Data;

import java.util.List;

@Data
public class DriverZoneDto {

    private Integer zoneId;
    private String zoneName;
    //private List<CoordinateDTO> boundary;
    // List 1: MultiPolygon -> List 2: Individual Polygons -> List 3: Rings -> CoordinateDTO
    private List<List<List<CoordinateDTO>>> boundary;
    private Integer createdBy;
    private String zoneType;

    @Data
    public static class CoordinateDTO {
        private double longitude;
        private double latitude;
    }
}
