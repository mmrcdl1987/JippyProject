package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DriverIncentiveSettingsResponseDto {

    private Integer driverIncentiveSettingsId;

    private Integer ordersCount;

    private BigDecimal incentiveAmount;

    private Integer zoneId;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}