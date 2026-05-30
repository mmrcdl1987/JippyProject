package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CoOutletDto {

    private Integer outletId;

    private String outletName;

    private Double distanceKm;
}