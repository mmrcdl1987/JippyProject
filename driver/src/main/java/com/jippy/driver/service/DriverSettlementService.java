package com.jippy.driver.service;

import com.jippy.driver.dto.DriverIncentiveSettlementResponseDto;
import com.jippy.driver.dto.DriverSettlementResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface DriverSettlementService {

//     for api "/getDriversSettlements"
//     for a given date range, get the total settlement amount for each driver, along with the
//     details of the incentives that contributed to that settlement. The details should
//     include the date, incentive amount, and the number of completed orders for that day.
    List<DriverSettlementResponseDto> getDriversSettlements(LocalDate startDate, LocalDate endDate);


//     for api "/getDriversIncentivesSettlements"
//    for a given filter (e.g., date range, driver ID), get the total incentives
//    amount for each driver,
    List<DriverIncentiveSettlementResponseDto> getDriversIncentivesForSettlements(String filter);
}

