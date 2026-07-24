package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoZoneResponseDto {

    private Integer zoneId;
    private String zoneName;
    private String zoneType;
    private JsonNode boundary;
}
