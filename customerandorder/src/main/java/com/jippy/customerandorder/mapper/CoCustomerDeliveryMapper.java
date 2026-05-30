package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoCustomerUnreachableRequestDto;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderRejection;

import java.time.LocalDateTime;

public class CoCustomerDeliveryMapper {

    private CoCustomerDeliveryMapper() {
    }

    public static CoOrderRejection mapToOrderRejectionEntity(CoCustomerUnreachableRequestDto requestDto, CoOrder order) {

        CoOrderRejection rejectionEntity = new CoOrderRejection();

        rejectionEntity.setOrderId(requestDto.getOrderId());

        // CUSTOMER CAUSED REJECTION
        rejectionEntity.setRejectedById(order.getCustomerId());

        rejectionEntity.setType("CUSTOMER");

        rejectionEntity.setReason(requestDto.getReason());

        rejectionEntity.setIsActive(true);

        rejectionEntity.setCreatedAt(LocalDateTime.now());

        // DRIVER WHO CREATED THIS ENTRY
        rejectionEntity.setCreatedBy(requestDto.getDriverId().intValue());

        return rejectionEntity;
    }
}