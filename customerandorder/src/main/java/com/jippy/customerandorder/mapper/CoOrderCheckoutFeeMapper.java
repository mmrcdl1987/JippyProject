package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoOrderCheckoutFeeRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutFeeResponseDto;
import com.jippy.customerandorder.entity.CoOrderCheckoutFee;
import org.springframework.stereotype.Component;

@Component
public class CoOrderCheckoutFeeMapper {

    public CoOrderCheckoutFee toEntity(CoOrderCheckoutFeeRequestDto request) {

        CoOrderCheckoutFee entity = new CoOrderCheckoutFee();

        entity.setPlatformFee(request.getPlatformFee());
        entity.setPlatformFeeToggle(request.getPlatformFeeToggle());

        entity.setSurgeFee(request.getSurgeFee());
        entity.setSurgeFeeToggle(request.getSurgeFeeToggle());

        entity.setPackagingFee(request.getPackagingFee());
        entity.setPackagingFeeToggle(request.getPackagingFeeToggle());

        entity.setAreaId(request.getAreaId());

        return entity;
    }

    public void updateEntity(CoOrderCheckoutFee entity, CoOrderCheckoutFeeRequestDto request) {

        entity.setPlatformFee(request.getPlatformFee());
        entity.setPlatformFeeToggle(request.getPlatformFeeToggle());

        entity.setSurgeFee(request.getSurgeFee());
        entity.setSurgeFeeToggle(request.getSurgeFeeToggle());

        entity.setPackagingFee(request.getPackagingFee());
        entity.setPackagingFeeToggle(request.getPackagingFeeToggle());

        entity.setAreaId(request.getAreaId());
    }

    public CoOrderCheckoutFeeResponseDto toResponse(CoOrderCheckoutFee entity) {

        return new CoOrderCheckoutFeeResponseDto(entity.getOrderCheckoutFeeId(),

                entity.getPlatformFee(), entity.getPlatformFeeToggle(),

                entity.getSurgeFee(), entity.getSurgeFeeToggle(),

                entity.getPackagingFee(), entity.getPackagingFeeToggle(),

                entity.getAreaId(),

                entity.getCreatedBy(), entity.getCreatedAt(),

                entity.getUpdatedBy(), entity.getUpdatedAt());
    }
}