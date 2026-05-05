package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmOutletByMerchantDto {
    // from  outlet table
    private Integer outletId;
    private String outletName;
    private String outletPhone;
    private Boolean isApproved;

    //    from location Entity (state,city,area) table
    private String stateName;
    private String cityName;
    private String areaName;
}
