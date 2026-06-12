package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DriverSettlementResponseDto {

    private Integer driverId;

    private Long noOfOrdersCompleted;

    private BigDecimal totalDriverEarnings;

    private List<DriverOrderSettlementDto> orders;
}