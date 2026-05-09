package com.jippy.customerandorder.iservice;


import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.dto.CoZoneDto;

public interface ICoDriverService {

    CoDriverDto postDriverDetails(CoDriverDto dto);

    CoDriverDto getDriverDetails(Integer driverId);

    String createZones(CoZoneDto zoneDto);

    CoDriverDto updateDriverDetails(Integer driverId, CoDriverDto dto);
}