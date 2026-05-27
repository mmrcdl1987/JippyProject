package com.jippy.driver.mapper;

import com.jippy.driver.dto.DeliveryChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DriverDeliveryChargeSettingsMapper {

    public DriverDeliveryChargeSettings mapToEntity(DriverDeliveryChargeSettingsRequestDto requestDto) {

        if (requestDto == null) {

            return null;
        }

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

        if (entity == null) {

            return null;
        }

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

    public DriverChargeCalculationResponseDto mapToDriverChargeResponse(BigDecimal pickupDistanceKm, BigDecimal deliveryDistanceKm, DriverDeliveryChargeSettings pickupSlab, DriverDeliveryChargeSettings deliverySlab, BigDecimal pickupCharge, BigDecimal deliveryCharge, BigDecimal taxAmount, BigDecimal totalDriverCharge, boolean codAvailable) {

        DriverChargeCalculationResponseDto response = new DriverChargeCalculationResponseDto();

        response.setPickupDistanceKm(pickupDistanceKm);

        response.setDeliveryDistanceKm(deliveryDistanceKm);

        response.setPickupUnitPrice(pickupSlab.getUnitPricePerPickKm());

        response.setDeliveryUnitPrice(deliverySlab.getUnitPricePerDeliverKm());

        response.setPickupCharge(pickupCharge);

        response.setDeliveryCharge(deliveryCharge);

        response.setTaxAmount(taxAmount);

        response.setTotalDriverCharge(totalDriverCharge);

        response.setCodAvailable(codAvailable);

        return response;
    }

    public DeliveryChargeCalculationResponseDto mapToDeliveryChargeResponse(BigDecimal deliveryDistanceKm, BigDecimal deliveryCharge, BigDecimal taxAmount, BigDecimal totalDeliveryCharge, boolean codAvailable) {

        DeliveryChargeCalculationResponseDto response = new DeliveryChargeCalculationResponseDto();

        response.setDeliveryDistanceKm(deliveryDistanceKm);

        response.setDeliveryCharge(deliveryCharge);

        response.setTaxAmount(taxAmount);

        response.setTotalDeliveryCharge(totalDeliveryCharge);

        response.setCodAvailable(codAvailable);

        return response;
    }
}