package com.jippy.driver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DriverEarningsDto {

    private Integer driverId;

    private LocalDate currentDate;

    private Long ordersCountToday;

    private BigDecimal totalEarningsToday;

//    after new requirement of driver incentive bonus we are adding this field
//    to show the total incentive bonus for the day in driver earnings api response
@JsonInclude(JsonInclude.Include.ALWAYS)
    private BigDecimal driverIncentiveBonus;
}