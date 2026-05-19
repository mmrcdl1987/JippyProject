package com.jippy.customerandorder.iservice;


import com.jippy.customerandorder.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ICoDriverService {

    CoDriverDto postDriverDetails(CoDriverDto dto);

    CoDriverDto getDriverDetails(Integer driverId);

    String createZones(CoZoneDto zoneDto);

    CoDriverDto updateDriverDetails(Integer driverId, CoDriverDto dto);

    CoDriverEarningsDto fetchEarnings(Integer driverId, LocalDate date);

    List<CoDriverOrderHistoryDto> fetchOrderEarningsHistory(Integer driverId);

    CoDriverTotalEarningsDto fetchTotalEarnings(Integer driverId);

    String driverDeliveredOrder(CoDriverOrderDto driverOrderDto);

}