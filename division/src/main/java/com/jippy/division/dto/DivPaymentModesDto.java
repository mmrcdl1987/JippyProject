package com.jippy.division.dto;

import lombok.Data;

@Data
public class DivPaymentModesDto {

    private Integer paymentModeId;
    private String paymentMode;
    private Integer createdBy;
    private Integer updatedBy;
    private String createdAt;
    private String updatedAt;
}
