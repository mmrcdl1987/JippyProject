package com.jippy.customerandorder.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoCustomerUnreachableResponseDto {

    private Boolean success;

    private String message;

    private Double distanceInMeters;

    private Boolean rejectionAllowed;
}