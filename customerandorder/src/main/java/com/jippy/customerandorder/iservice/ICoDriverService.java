package com.jippy.customerandorder.iservice;


import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.dto.CoDriverEarningsDto;
import com.jippy.customerandorder.dto.CoZoneDto;

import java.time.LocalDate;

public interface ICoDriverService {

    CoDriverDto postDriverDetails(CoDriverDto dto);

    CoDriverDto getDriverDetails(Integer driverId);

    String createZones(CoZoneDto zoneDto);

    CoDriverDto updateDriverDetails(Integer driverId, CoDriverDto dto);

    CoDriverEarningsDto fetchEarnings(Integer driverId, LocalDate date);
}