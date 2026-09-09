package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoOrderCheckoutTaxRequestDto;
import com.jippy.customerandorder.dto.CoOrderCheckoutTaxResponseDto;
import com.jippy.customerandorder.entity.CoOrderCheckoutTax;
import org.springframework.stereotype.Component;

@Component
public class CoOrderCheckoutTaxMapper {

    public CoOrderCheckoutTax toEntity(CoOrderCheckoutTaxRequestDto request) {

        CoOrderCheckoutTax entity = new CoOrderCheckoutTax();

        entity.setPlatformFeeTax(request.getPlatformFeeTax());
        entity.setSurgeFeeTax(request.getSurgeFeeTax());
        entity.setPackagingFeeTax(request.getPackagingFeeTax());
        entity.setDeliveryFeeTax(request.getDeliveryFeeTax());
        entity.setFoodAmountTax(request.getFoodAmountTax());

        return entity;
    }

    public void updateEntity(CoOrderCheckoutTax entity, CoOrderCheckoutTaxRequestDto request) {

        entity.setPlatformFeeTax(request.getPlatformFeeTax());
        entity.setSurgeFeeTax(request.getSurgeFeeTax());
        entity.setPackagingFeeTax(request.getPackagingFeeTax());
        entity.setDeliveryFeeTax(request.getDeliveryFeeTax());
        entity.setFoodAmountTax(request.getFoodAmountTax());
    }

    public CoOrderCheckoutTaxResponseDto toResponse(CoOrderCheckoutTax entity) {

        return new CoOrderCheckoutTaxResponseDto(entity.getOrderCheckoutTaxId(), entity.getPlatformFeeTax(), entity.getSurgeFeeTax(), entity.getPackagingFeeTax(), entity.getDeliveryFeeTax(), entity.getFoodAmountTax(), entity.getCreatedBy(), entity.getCreatedAt(), entity.getUpdatedBy(), entity.getUpdatedAt());
    }
}