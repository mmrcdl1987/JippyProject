package com.jippy.driver.service;

import com.jippy.driver.dto.DriverIncentiveHistoryPageResponseDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface DriverIncentiveSettingsService {

    Page<DriverIncentiveHistoryPageResponseDto> getIncentiveHistoryPage(
            Integer driverId,
            String filter,
            LocalDate startDate,
            LocalDate endDate,
            Integer page,
            Integer size
    );
}