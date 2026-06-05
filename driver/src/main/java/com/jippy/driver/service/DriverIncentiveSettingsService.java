package com.jippy.driver.service;


import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import org.springframework.data.domain.Page;

public interface DriverIncentiveSettingsService {

    DriverIncentiveSettingsDto saveOrUpdateIncentives(DriverIncentiveSettingsDto dto);

//    based on the filter value, we can get the
//    incentive history for a driver for a day, week or month
//    either filter value can be "daily", "weekly" or "monthly" or ALL
    Page<DriverIncentiveHistoryResponseDto> getDriverIncentiveHistory
    (Integer driverId, String filter,Integer page,
     Integer size);
}