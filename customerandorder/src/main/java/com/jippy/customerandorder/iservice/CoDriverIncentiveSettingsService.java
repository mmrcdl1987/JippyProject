package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoDriverIncentiveSettingsDto;

public interface CoDriverIncentiveSettingsService {

    CoDriverIncentiveSettingsDto  saveOrUpdateIncentives(CoDriverIncentiveSettingsDto dto);
}