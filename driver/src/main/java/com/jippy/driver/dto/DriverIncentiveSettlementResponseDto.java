package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DriverIncentiveSettlementResponseDto {

    private Integer driverId;

    private BigDecimal totalIncentivesAmount;

    private List<DriverIncentiveDetailDto> incentives;
}