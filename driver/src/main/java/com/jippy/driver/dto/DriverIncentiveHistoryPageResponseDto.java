package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DriverIncentiveHistoryPageResponseDto {

    private Integer driverIncentiveHistoryId;

    private Integer driverId;

    private String driverName;

    private LocalDate currDate;

    private BigDecimal incentiveAmount;

    private Integer completedOrdersCount;

    private LocalDateTime createdAt;
}