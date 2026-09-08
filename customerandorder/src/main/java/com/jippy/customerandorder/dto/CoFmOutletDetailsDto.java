package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents outlet information received from FM microservice.
 */
@Getter
@Setter
public class CoFmOutletDetailsDto {

    private Integer outletId;

    private String outletName;

    private String areaName;
}