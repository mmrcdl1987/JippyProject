package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FMCityDto {

    private Integer cityId;
    private String cityName;
    private Integer stateId;
}