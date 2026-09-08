package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used to fetch multiple outlet details.
 */
@Getter
@Setter
public class FmOutletDetailsRequestDto {

    private List<Integer> outletIds;
}