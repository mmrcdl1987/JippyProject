package com.jippy.driver.dto;

import lombok.Data;

import java.util.List;

@Data
public class DriverZoneDto {

    private Integer zoneId;

    private String zoneName;

    // Y = Active, N = Inactive
    private String status;

    // MultiPolygon -> Polygons -> Rings -> Coordinates
    private List<List<List<CoordinateDTO>>> boundary;

    private Integer createdBy;

    @Data
    public static class CoordinateDTO {

        private double longitude;

        private double latitude;
    }
}