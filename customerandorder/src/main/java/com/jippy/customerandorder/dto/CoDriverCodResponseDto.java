package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

//Response DTO for COD processing API
@Data
public class CoDriverCodResponseDto {

    private String message;
    private Integer driverId;
    private String orderId;
    private BigDecimal deductedAmount;
    private BigDecimal remainingCodAmount;
    private Boolean ordersLock;
}