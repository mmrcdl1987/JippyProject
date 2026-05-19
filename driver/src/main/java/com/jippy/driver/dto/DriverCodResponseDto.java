package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;

//Response DTO for COD processing API
@Data
public class DriverCodResponseDto {

    private String message;
    private Integer driverId;
    private String orderId;
    private BigDecimal deductedAmount;
    private BigDecimal remainingCodAmount;
    private Boolean ordersLock;
}