package com.jippy.driver.dto;

import lombok.Data;

@Data
public class UpdateDriverLocationDto {

    private Integer driverId;
    private double latitude;
    private double longitude;
    private String orderId;

    @Override
    public String toString() {
        return "CoUpdateDriverLocationDto{" +
                "driverId=" + driverId +
                ", Latitude=" + latitude +
                ", Longitude=" + longitude +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}
