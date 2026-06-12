package com.jippy.driver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DriverIncentiveDetailDto {

    private LocalDate currDate;

    private BigDecimal incentiveAmount;

    private Integer completedOrdersCount;
}