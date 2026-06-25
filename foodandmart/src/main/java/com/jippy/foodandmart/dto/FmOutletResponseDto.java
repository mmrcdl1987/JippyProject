package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmOutletResponseDto {

    private Integer outletId;
    private String outletName;
    private String outletEmail;
    private Integer merchantId;
    private String cuisineType;
    private String outletPhone;
    private BigDecimal radius;
    private String isActive;
    private Boolean isApproved;

//    From Location Column
    private Double latitude;
    private Double longitude;
}