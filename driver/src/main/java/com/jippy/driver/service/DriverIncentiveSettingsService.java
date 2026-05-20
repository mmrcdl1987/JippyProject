package com.jippy.driver.service;


import com.jippy.driver.dto.DriverIncentiveSettingsDto;

public interface DriverIncentiveSettingsService {

    DriverIncentiveSettingsDto saveOrUpdateIncentives(DriverIncentiveSettingsDto dto);
}