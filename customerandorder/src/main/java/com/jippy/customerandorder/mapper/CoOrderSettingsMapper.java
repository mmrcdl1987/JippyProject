package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.entity.OrderSettings;
import org.springframework.stereotype.Component;

@Component
public class CoOrderSettingsMapper {

    public CoOrderSettingsResponseDto mapToResponse(OrderSettings orderSettings, String message) {

        CoOrderSettingsResponseDto response = new CoOrderSettingsResponseDto();

        response.setOrderSettingsId(orderSettings.getOrderSettingsId());

        response.setPlatformFee(orderSettings.getPlatformFee());

        response.setSurgeFee(orderSettings.getSurgeFee());

        response.setPackagingFee(orderSettings.getPackagingFee());

        response.setDeliveryFeeTax(orderSettings.getDeliveryFeeTax());

        response.setFoodTotalAmountTax(orderSettings.getFoodTotalAmountTax());

        response.setCreatedBy(orderSettings.getCreatedBy());

        response.setCreatedAt(orderSettings.getCreatedAt());

        response.setUpdatedBy(orderSettings.getUpdatedBy());

        response.setUpdatedAt(orderSettings.getUpdatedAt());

        response.setMessage(message);

        return response;
    }
}