package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmOutletCompleteDetailsDto {

    private Integer outletId;
    private String outletName;
    private String outletPhone;
    private String outletPicUrl;
    private String buildingNumber;
}