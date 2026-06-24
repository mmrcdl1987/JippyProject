package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FmDriverDto {

    private Integer driverId;

    private String email;
}