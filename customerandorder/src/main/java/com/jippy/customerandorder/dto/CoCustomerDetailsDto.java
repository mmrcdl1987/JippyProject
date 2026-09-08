package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerDetailsDto {

    private Integer customerId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private String buildingName;
}