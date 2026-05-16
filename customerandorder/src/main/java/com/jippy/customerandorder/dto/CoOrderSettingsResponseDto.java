package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CoOrderSettingsResponseDto {

    private Integer orderSettingsId;

    private BigDecimal platformFee;

    private BigDecimal surgeFee;

    private BigDecimal packagingFee;

    private BigDecimal deliveryFeeTax;

    private BigDecimal foodTotalAmountTax;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;

    private String message;
}