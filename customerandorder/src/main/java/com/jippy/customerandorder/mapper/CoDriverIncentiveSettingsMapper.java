/*
package com.jippy.customerandorder.mapper;


import com.jippy.customerandorder.dto.CoDriverIncentiveSettingsDto;
import com.jippy.customerandorder.entity.CoDriverIncentiveSettings;

public class CoDriverIncentiveSettingsMapper {

//    to post new entity from DTO, including ID if present (for update scenarios)
    public static CoDriverIncentiveSettings toIncentiveEntity(CoDriverIncentiveSettingsDto dto) {

        CoDriverIncentiveSettings entity = new CoDriverIncentiveSettings();

        entity.setDriverIncentiveSettingsId(dto.getDriverIncentiveSettingsId());
        entity.setOrdersCount(dto.getOrdersCount());
        entity.setIncentiveAmount(dto.getIncentiveAmount());

        return entity;
    }

//    to update existing entity with new values from DTO, without changing the ID
    public static void updateIncentiveEntity(CoDriverIncentiveSettings entity, CoDriverIncentiveSettingsDto dto) {

        entity.setOrdersCount(dto.getOrdersCount());
        entity.setIncentiveAmount(dto.getIncentiveAmount());
    }

    // convert entity to DTO (for response)
    public static CoDriverIncentiveSettingsDto incentiveEntityToDto(CoDriverIncentiveSettings entity) {

        CoDriverIncentiveSettingsDto dto = new CoDriverIncentiveSettingsDto();

        dto.setDriverIncentiveSettingsId(entity.getDriverIncentiveSettingsId());
        dto.setOrdersCount(entity.getOrdersCount());
        dto.setIncentiveAmount(entity.getIncentiveAmount());

        return dto;
    }
}*/
