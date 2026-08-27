package com.jippy.driver.mapper;

import com.jippy.driver.dto.DeliveryChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsListResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import com.jippy.driver.feignClients.FMFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.jippy.driver.dto.FMAreaDto;
import com.jippy.driver.dto.FMCityDto;
import com.jippy.driver.dto.FMStateDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DriverDeliveryChargeSettingsMapper {
    private final FMFeignClient fmFeignClient;

    // =========================================================
    // REQUEST DTO -> ENTITY
    // Existing method - NO CHANGE
    // =========================================================

    public DriverDeliveryChargeSettings mapToEntity(DriverDeliveryChargeSettingsRequestDto requestDto) {

        if (requestDto == null) {
            return null;
        }

        DriverDeliveryChargeSettings entity = new DriverDeliveryChargeSettings();

//        entity.setPickUpKmsRangeFrom(requestDto.getPickUpKmsRangeFrom());
//
//        entity.setPickUpKmsRangeTo(requestDto.getPickUpKmsRangeTo());
//
//        entity.setUnitPricePerPickKm(requestDto.getUnitPricePerPickKm());
//
//        entity.setDeliveryKmsRangeFrom(requestDto.getDeliveryKmsRangeFrom());
//
//        entity.setDeliveryKmsRangeTo(requestDto.getDeliveryKmsRangeTo());
//
//        entity.setUnitPricePerDeliverKm(requestDto.getUnitPricePerDeliverKm());
//
//        entity.setCreatedBy(requestDto.getCreatedBy());
//
//
//        entity.setAreaId(requestDto.getAreaId());

        return entity;
    }


    // =========================================================
    // ENTITY -> EXISTING SETTINGS RESPONSE DTO
    // Existing API - NO CHANGE
    // =========================================================

    public DriverDeliveryChargeSettingsResponseDto mapToResponseDto(DriverDeliveryChargeSettings entity) {

        if (entity == null) {
            return null;
        }

        DriverDeliveryChargeSettingsResponseDto responseDto = new DriverDeliveryChargeSettingsResponseDto();

        responseDto.setDeliveryChargeSettingId(entity.getDeliveryChargeSettingId());

//        responseDto.setPickUpKmsRangeFrom(entity.getPickUpKmsRangeFrom());
//
//        responseDto.setPickUpKmsRangeTo(entity.getPickUpKmsRangeTo());
//
//        responseDto.setUnitPricePerPickKm(entity.getUnitPricePerPickKm());
//
//        responseDto.setDeliveryKmsRangeFrom(entity.getDeliveryKmsRangeFrom());
//
//        responseDto.setDeliveryKmsRangeTo(entity.getDeliveryKmsRangeTo());
//
//        responseDto.setUnitPricePerDeliverKm(entity.getUnitPricePerDeliverKm());

        responseDto.setCreatedAt(entity.getCreatedAt());

        responseDto.setCreatedBy(entity.getCreatedBy());

        return responseDto;
    }


    // =========================================================
    // ENTITY -> NEW LIST RESPONSE DTO
    //
    // Used by:
    // GET /api/driver/delivery-charge/settings
    //
    // IMPORTANT:
    // State/City/Area names belong to FM MS.
    // This mapper only maps data available in Driver MS.
    // =========================================================

    public DriverDeliveryChargeSettingsListResponseDto mapToListResponseDto(
            DriverDeliveryChargeSettings entity) {

        if (entity == null) {
            return null;
        }

        DriverDeliveryChargeSettingsListResponseDto response =
                new DriverDeliveryChargeSettingsListResponseDto();

        // =====================================================
        // DELIVERY CHARGE SETTING
        // =====================================================

        response.setDeliveryChargeSettingId(
                entity.getDeliveryChargeSettingId()
        );


        // =====================================================
        // AREA
        // =====================================================

//        response.setAreaId(
//                entity.getAreaId()
//        );


        // =====================================================
        // STATE / CITY / AREA NAMES
        //
        // These are maintained in FM service.
        // They will be populated in ServiceImpl
        // using FMFeignClient.
        // =====================================================

        response.setStateId(null);
        response.setStateName(null);

        response.setCityId(null);
        response.setCityName(null);

        response.setAreaName(null);


        // =====================================================
        // PICKUP SETTINGS
        // =====================================================

//        response.setPickUpKmsRangeFrom(
//                entity.getPickUpKmsRangeFrom()
//        );
//
//        response.setPickUpKmsRangeTo(
//                entity.getPickUpKmsRangeTo()
//        );
//
//        response.setUnitPricePerPickKm(
//                entity.getUnitPricePerPickKm()
//        );


        // =====================================================
        // DELIVERY SETTINGS
        // =====================================================

//        response.setDeliveryKmsRangeFrom(
//                entity.getDeliveryKmsRangeFrom()
//        );
//
//        response.setDeliveryKmsRangeTo(
//                entity.getDeliveryKmsRangeTo()
//        );
//
//        response.setUnitPricePerDeliverKm(
//                entity.getUnitPricePerDeliverKm()
//        );


        // =====================================================
        // AUDIT FIELDS
        // =====================================================

        response.setCreatedAt(
                entity.getCreatedAt()
        );

        response.setCreatedBy(
                entity.getCreatedBy()
        );

        response.setUpdatedAt(
                entity.getUpdatedAt()
        );

        response.setUpdatedBy(
                entity.getUpdatedBy()
        );


        return response;
    }


    // =========================================================
    // DRIVER CHARGE CALCULATION RESPONSE
    // Existing method - NO CHANGE
    // =========================================================

    public DriverChargeCalculationResponseDto mapToDriverChargeResponse(BigDecimal pickupDistanceKm, BigDecimal deliveryDistanceKm, DriverDeliveryChargeSettings pickupSlab, DriverDeliveryChargeSettings deliverySlab, BigDecimal pickupCharge, BigDecimal deliveryCharge, BigDecimal taxAmount, BigDecimal totalDriverCharge, boolean codAvailable) {

        DriverChargeCalculationResponseDto response = new DriverChargeCalculationResponseDto();

        response.setPickupDistanceKm(pickupDistanceKm);

        response.setDeliveryDistanceKm(deliveryDistanceKm);
//
//        response.setPickupUnitPrice(pickupSlab.getUnitPricePerPickKm());
//
//        response.setDeliveryUnitPrice(deliverySlab.getUnitPricePerDeliverKm());

        response.setPickupCharge(pickupCharge);

        response.setDeliveryCharge(deliveryCharge);

        response.setTaxAmount(taxAmount);

        response.setTotalDriverCharge(totalDriverCharge);

        response.setCodAvailable(codAvailable);

        return response;
    }


    // =========================================================
    // DELIVERY CHARGE CALCULATION RESPONSE
    // Existing method - NO CHANGE
    // =========================================================

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