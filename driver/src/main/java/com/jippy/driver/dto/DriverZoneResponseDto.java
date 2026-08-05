package com.jippy.driver.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverZoneResponseDto {

    private Integer zoneId;
    private String zoneName;
    private JsonNode boundary;
}
