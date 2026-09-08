package com.jippy.driver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used to fetch multiple driver details.
 */
@Getter
@Setter
public class DriverDetailsRequestDto {

    private List<Integer> driverIds;
}