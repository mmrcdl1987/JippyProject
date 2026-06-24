package com.jippy.driver.service;


import com.jippy.driver.dto.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface DriverService {

    DriverDto postDriverDetails(DriverDto dto);

    DriverDto getDriverDetails(Integer driverId);

    String createZones(DriverZoneDto zoneDto);

    DriverDto updateDriverDetails(Integer driverId, DriverDto dto);

    DriverEarningsDto fetchEarnings(Integer driverId, LocalDate date);

    List<DriverOrderHistoryDto> fetchOrderEarningsHistory(Integer driverId);

    DriverTotalEarningsDto fetchTotalEarnings(Integer driverId);

    String driverDeliveredOrder(DriverOrderDto driverOrderDto);

    String saveOrUpdateProfilePic(@Valid UploadProfilePicDto uploadProfilePicDto);

    DriverDto findByEmail(String email);
}