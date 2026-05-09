package com.jippy.customerandorder.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoZoneDto {

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
