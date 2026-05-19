package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoUpdateDriverLocationDto {

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
