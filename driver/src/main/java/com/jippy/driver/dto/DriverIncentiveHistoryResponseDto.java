package com.jippy.driver.dto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DriverIncentiveHistoryResponseDto {

    private LocalDate date;

    private Integer driverId;

    private Integer noOfOrders;

    private BigDecimal incentiveAmount;
}