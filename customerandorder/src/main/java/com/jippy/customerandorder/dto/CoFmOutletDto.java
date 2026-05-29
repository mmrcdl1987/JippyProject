package com.jippy.customerandorder.dto;

import lombok.Data;

// Outlet response from FM microservice
@Data
public class CoFmOutletDto {

    //    From FM microservice, we will get outlet details like id, name, phone and area id.
//    We can use these details in order creation and settlement process.
    private Integer outletId;

    private String outletName;

    private String outletPhone;

    private Integer areaId;

    private String areaName;
}