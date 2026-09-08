package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoOutletDetailsDto {

    private Integer outletId;
    private String outletName;
    private String outletPhone;
    private String outletPicUrl;
    private String buildingNumber;
}