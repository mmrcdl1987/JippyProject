package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CoDriverEarningsDto {

    private Integer driverId;

    private LocalDate currentDate;

    private Long ordersCountToday;

    private BigDecimal totalEarningsToday;
}