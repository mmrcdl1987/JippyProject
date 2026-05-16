package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.CreateOutletUnavailabilityRequestDto;
import com.jippy.foodandmart.entity.OutletUnavailability;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OutletUnavailabilityMapper {

    public OutletUnavailability mapToEntity(CreateOutletUnavailabilityRequestDto requestDto) {

        OutletUnavailability entity = new OutletUnavailability();

        entity.setType(requestDto.getType().toUpperCase());

        entity.setUnavailabilityId(requestDto.getUnavailabilityId());

        entity.setUnavailabilityFromDate(requestDto.getUnavailabilityFromDate());

        entity.setUnavailabilityToDate(requestDto.getUnavailabilityToDate());

        entity.setReason(requestDto.getReason());


        entity.setCreatedAt(LocalDateTime.now());

        entity.setCreatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        return entity;
    }

    public OutletUnavailability updateEntity(OutletUnavailability entity, CreateOutletUnavailabilityRequestDto requestDto) {

        entity.setType(requestDto.getType().toUpperCase());

        entity.setUnavailabilityId(requestDto.getUnavailabilityId());

        entity.setUnavailabilityFromDate(requestDto.getUnavailabilityFromDate());

        entity.setUnavailabilityToDate(requestDto.getUnavailabilityToDate());

        entity.setReason(requestDto.getReason());


        entity.setUpdatedAt(LocalDateTime.now());

        entity.setUpdatedBy(FmAppConstants.DEFAULT_CREATED_BY);

        return entity;
    }
}