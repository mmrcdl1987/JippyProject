package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerDeliveryAddressResponseDto {

    private Integer customerAddressId;

    private Integer customerId;

    private Double latitude;

    private Double longitude;

    private String doorNo;

    private String buildingName;

    private String laneNo;

    private Integer area;

    private Integer city;
}