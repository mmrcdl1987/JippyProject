package com.jippy.customerandorder.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CoNearbyOutletResponseDto {

    private Integer totalOutlets;

    private List<CoOutletDto> outlets;
}