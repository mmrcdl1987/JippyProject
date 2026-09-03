package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoPaymentModesDto {

    private Integer paymentModeId;
    private String paymentMode;
    private Integer createdBy;
    private Integer updatedBy;
    private String createdAt;
    private String updatedAt;
    private String isActive;
}
