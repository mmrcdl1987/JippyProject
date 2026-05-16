package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.customerandorder.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.customerandorder.entity.DriverDeliveryChargeSettings;
import org.springframework.stereotype.Component;

@Component
public class DriverDeliveryChargeSettingsMapper {

    public DriverDeliveryChargeSettings mapToEntity(DriverDeliveryChargeSettingsRequestDto requestDto) {

        DriverDeliveryChargeSettings entity = new DriverDeliveryChargeSettings();

        entity.setPickUpKmsRangeFrom(requestDto.getPickUpKmsRangeFrom());
        entity.setPickUpKmsRangeTo(requestDto.getPickUpKmsRangeTo());
        entity.setUnitPricePerPickKm(requestDto.getUnitPricePerPickKm());
        entity.setDeliveryKmsRangeFrom(requestDto.getDeliveryKmsRangeFrom());
        entity.setDeliveryKmsRangeTo(requestDto.getDeliveryKmsRangeTo());
        entity.setUnitPricePerDeliverKm(requestDto.getUnitPricePerDeliverKm());
        entity.setCreatedBy(requestDto.getCreatedBy());

        return entity;
    }

    public DriverDeliveryChargeSettingsResponseDto mapToResponseDto(DriverDeliveryChargeSettings entity) {

        DriverDeliveryChargeSettingsResponseDto responseDto = new DriverDeliveryChargeSettingsResponseDto();

        responseDto.setDeliveryChargeSettingId(entity.getDeliveryChargeSettingId());

        responseDto.setPickUpKmsRangeFrom(entity.getPickUpKmsRangeFrom());

        responseDto.setPickUpKmsRangeTo(entity.getPickUpKmsRangeTo());

        responseDto.setUnitPricePerPickKm(entity.getUnitPricePerPickKm());

        responseDto.setDeliveryKmsRangeFrom(entity.getDeliveryKmsRangeFrom());

        responseDto.setDeliveryKmsRangeTo(entity.getDeliveryKmsRangeTo());

        responseDto.setUnitPricePerDeliverKm(entity.getUnitPricePerDeliverKm());

        responseDto.setCreatedAt(entity.getCreatedAt());

        responseDto.setCreatedBy(entity.getCreatedBy());

        return responseDto;
    }
}