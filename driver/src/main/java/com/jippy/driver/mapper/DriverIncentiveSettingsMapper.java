package com.jippy.driver.mapper;


import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import com.jippy.driver.entity.DriverIncentiveHistory;
import com.jippy.driver.entity.DriverIncentiveSettings;

public class DriverIncentiveSettingsMapper {

    //    to post new entity from DTO, including ID if present (for update scenarios)
    public static DriverIncentiveSettings toIncentiveEntity(DriverIncentiveSettingsDto dto) {

        DriverIncentiveSettings entity = new DriverIncentiveSettings();

        entity.setDriverIncentiveSettingsId(dto.getDriverIncentiveSettingsId());
        entity.setOrdersCount(dto.getOrdersCount());
        entity.setIncentiveAmount(dto.getIncentiveAmount());

        return entity;
    }

    //    to update existing entity with new values from DTO, without changing the ID
    public static void updateIncentiveEntity(DriverIncentiveSettings entity, DriverIncentiveSettingsDto dto) {

        entity.setOrdersCount(dto.getOrdersCount());
        entity.setIncentiveAmount(dto.getIncentiveAmount());
    }

    // convert entity to DTO (for response)
    public static DriverIncentiveSettingsDto incentiveEntityToDto(DriverIncentiveSettings entity) {

        DriverIncentiveSettingsDto dto = new DriverIncentiveSettingsDto();

        dto.setDriverIncentiveSettingsId(entity.getDriverIncentiveSettingsId());
        dto.setOrdersCount(entity.getOrdersCount());
        dto.setIncentiveAmount(entity.getIncentiveAmount());

        return dto;
    }

//    to convert DriverIncentiveHistory entity to DriverIncentiveHistoryResponseDto for response
    public static DriverIncentiveHistoryResponseDto toResponseDto(DriverIncentiveHistory entity) {

        DriverIncentiveHistoryResponseDto dto = new DriverIncentiveHistoryResponseDto();

        dto.setDate(entity.getCurrDate());

        dto.setDriverId(entity.getDriverId());

        dto.setNoOfOrders(entity.getCompletedOrdersCount());

        dto.setIncentiveAmount(entity.getIncentiveAmount());

        return dto;
    }
}