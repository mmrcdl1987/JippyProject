package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmMerchantAddressDto {

    private Integer addressId;
    private Integer merchantId;
    private String addressType;
    private String buildingNumber;
    private String road;
    private String landmark;
    
    // Location IDs
    private Integer stateId;
    private Integer cityId;
    private Integer areaId;
    
    // Location Names
    private String stateName;
    private String cityName;
    private String areaName;
}
