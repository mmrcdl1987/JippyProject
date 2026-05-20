package com.jippy.driver.dto;

import lombok.Data;

import java.util.List;

@Data
public class DriverZoneDto {

    private Integer zoneId;
    private String zoneName;
    private List<CoordinateDTO> boundary;
    private Integer createdBy;

    @Data
    public static class CoordinateDTO {
        private double longitude;
        private double latitude;
    }
}
