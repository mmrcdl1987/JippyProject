package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO containing outlet and area information.
 */
@Getter
@Setter
public class FmOutletDetailsResponseDto {

    private Integer outletId;

    private String outletName;

    private String areaName;
}