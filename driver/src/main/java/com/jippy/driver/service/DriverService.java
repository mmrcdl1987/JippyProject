package com.jippy.driver.service;


import com.jippy.driver.dto.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface DriverService {

    DriverDto postDriverDetails(DriverDto dto);

    DriverDto getDriverDetails(Integer driverId);

    List<DriverDto> getAllDrivers();

    /**
     * Approves the driver.
     *
     * @param driverId Driver Id
     */
    void approveDriver(Integer driverId);

    String createZones(DriverZoneDto zoneDto);

    DriverDto updateDriverDetails(Integer driverId, DriverDto dto);

    DriverEarningsDto fetchEarnings(Integer driverId, LocalDate date);

    List<DriverOrderHistoryDto> fetchOrderEarningsHistory(Integer driverId);

    DriverTotalEarningsDto fetchTotalEarnings(Integer driverId);

    String driverDeliveredOrder(DriverOrderDto driverOrderDto);

    String saveOrUpdateProfilePic(@Valid UploadProfilePicDto uploadProfilePicDto);

    DriverDto findByEmail(String email);

    /**
     * ===========================================================
     * Fetch Driver Details by Driver Id
     * ===========================================================
     *
     * Retrieves complete Driver information required for
     * the FM Approval workflow.
     *
     * @param driverId Driver Id
     * @return Driver Approval Response
     */
    FmDriverApprovalResponseDTO getDriverById(Integer driverId);

    List<DriverZoneResponseDto> getZones();

//    DriverZoneResponseDto findCommunityById(Integer communityId);
//
//    Integer findCustomerInCommunity(Double latitude, Double longitude);
//
//    Integer checkCustomerAddressWithCommunity(Double latitude, Double longitude, Integer communityId);
}