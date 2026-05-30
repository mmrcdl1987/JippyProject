package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.entity.CoOrderSettings;
import org.springframework.stereotype.Component;

@Component
public class CoOrderSettingsMapper {

    public CoOrderSettingsResponseDto mapToResponse(CoOrderSettings coOrderSettings, String message) {

        CoOrderSettingsResponseDto response = new CoOrderSettingsResponseDto();

        response.setOrderSettingsId(coOrderSettings.getOrderSettingsId());

        response.setPlatformFee(coOrderSettings.getPlatformFee());

        response.setSurgeFee(coOrderSettings.getSurgeFee());

        response.setPackagingFee(coOrderSettings.getPackagingFee());

        response.setDeliveryFeeTax(coOrderSettings.getDeliveryFeeTax());

        response.setFoodTotalAmountTax(coOrderSettings.getFoodTotalAmountTax());

        response.setCreatedBy(coOrderSettings.getCreatedBy());

        response.setCreatedAt(coOrderSettings.getCreatedAt());

        response.setUpdatedBy(coOrderSettings.getUpdatedBy());

        response.setUpdatedAt(coOrderSettings.getUpdatedAt());

        response.setMessage(message);

        return response;
    }
}