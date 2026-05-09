package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoOrderRejectionRequestDto;
import com.jippy.customerandorder.entity.CoOrderRejection;

import java.time.LocalDateTime;

/**
 * Mapper: CoOrderRejectionMapper
 */
public class CoOrderRejectionMapper {

    public static CoOrderRejection toEntity(CoOrderRejectionRequestDto dto) {

        CoOrderRejection entity = new CoOrderRejection();

        entity.setOrderId(dto.getOrderId());
        entity.setRejectedById(dto.getRejectedById());
        entity.setType(dto.getType().toUpperCase());
        entity.setReason(dto.getReason());

        entity.setIsActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(dto.getRejectedById());

        return entity;
    }
}