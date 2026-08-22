package com.jippy.driver.mapper;

import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetAllResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetByIdResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsSaveRequestDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import org.springframework.stereotype.Component;

@Component
public class DriverMsDeliveryChargeSettingsMapper {

    // ============================================================
    // SAVE REQUEST -> ENTITY
    // Used for CREATE
    // ============================================================

    public DriverDeliveryChargeSettings toEntity(
            DriverDeliveryChargeSettingsSaveRequestDto request) {

        if (request == null) {
            return null;
        }

        DriverDeliveryChargeSettings entity =
                new DriverDeliveryChargeSettings();

        entity.setKmsRangeFrom(request.getKmsRangeFrom());
        entity.setKmsRangeTo(request.getKmsRangeTo());
        entity.setUnitPricePerKm(request.getUnitPricePerKm());

        entity.setChargeType(request.getChargeType());
        entity.setDeliveryType(request.getDeliveryType());
        entity.setDriverType(request.getDriverType());
        entity.setServiceType(request.getServiceType());
        entity.setVehicleType(request.getVehicleType());
        entity.setFuelType(request.getFuelType());

        entity.setZoneId(request.getZoneId());
        entity.setCurrencyCode(request.getCurrencyCode());

        entity.setWaitingFreeMinutes(request.getWaitingFreeMinutes());
        entity.setWaitingPerMinute(request.getWaitingPerMinute());

        entity.setNightCharge(request.getNightCharge());
        entity.setPeakCharge(request.getPeakCharge());
        entity.setWeatherSurcharge(request.getWeatherSurcharge());
        entity.setRemoteAreaCharge(request.getRemoteAreaCharge());
        entity.setRemoteZoneSurcharge(request.getRemoteZoneSurcharge());

        entity.setStatus(request.getStatus());

        return entity;
    }

    // ============================================================
    // SAVE REQUEST -> EXISTING ENTITY
    // Used for UPDATE
    // ============================================================

    public void updateEntity(
            DriverDeliveryChargeSettings entity,
            DriverDeliveryChargeSettingsSaveRequestDto request) {

        entity.setKmsRangeFrom(request.getKmsRangeFrom());
        entity.setKmsRangeTo(request.getKmsRangeTo());
        entity.setUnitPricePerKm(request.getUnitPricePerKm());

        entity.setChargeType(request.getChargeType());
        entity.setDeliveryType(request.getDeliveryType());
        entity.setDriverType(request.getDriverType());
        entity.setServiceType(request.getServiceType());
        entity.setVehicleType(request.getVehicleType());
        entity.setFuelType(request.getFuelType());

        entity.setZoneId(request.getZoneId());
        entity.setCurrencyCode(request.getCurrencyCode());

        entity.setWaitingFreeMinutes(request.getWaitingFreeMinutes());
        entity.setWaitingPerMinute(request.getWaitingPerMinute());

        entity.setNightCharge(request.getNightCharge());
        entity.setPeakCharge(request.getPeakCharge());
        entity.setWeatherSurcharge(request.getWeatherSurcharge());
        entity.setRemoteAreaCharge(request.getRemoteAreaCharge());
        entity.setRemoteZoneSurcharge(request.getRemoteZoneSurcharge());

        entity.setStatus(request.getStatus());
    }

    // ============================================================
    // ENTITY -> GET BY ID RESPONSE
    // ============================================================

    public DriverDeliveryChargeSettingsGetByIdResponseDto
    toGetByIdResponseDto(DriverDeliveryChargeSettings entity) {

        if (entity == null) {
            return null;
        }

        DriverDeliveryChargeSettingsGetByIdResponseDto response =
                new DriverDeliveryChargeSettingsGetByIdResponseDto();

        response.setDeliveryChargeSettingId(
                entity.getDeliveryChargeSettingId()
        );

        response.setKmsRangeFrom(entity.getKmsRangeFrom());
        response.setKmsRangeTo(entity.getKmsRangeTo());
        response.setUnitPricePerKm(entity.getUnitPricePerKm());

        response.setChargeType(entity.getChargeType());
        response.setDeliveryType(entity.getDeliveryType());
        response.setDriverType(entity.getDriverType());
        response.setServiceType(entity.getServiceType());
        response.setVehicleType(entity.getVehicleType());
        response.setFuelType(entity.getFuelType());

        response.setZoneId(entity.getZoneId());
        response.setCurrencyCode(entity.getCurrencyCode());

        response.setWaitingFreeMinutes(entity.getWaitingFreeMinutes());
        response.setWaitingPerMinute(entity.getWaitingPerMinute());

        response.setNightCharge(entity.getNightCharge());
        response.setPeakCharge(entity.getPeakCharge());
        response.setWeatherSurcharge(entity.getWeatherSurcharge());
        response.setRemoteAreaCharge(entity.getRemoteAreaCharge());
        response.setRemoteZoneSurcharge(entity.getRemoteZoneSurcharge());

        response.setStatus(entity.getStatus());

        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());

        return response;
    }

    // ============================================================
    // ENTITY -> GET ALL RESPONSE
    // ============================================================

    public DriverDeliveryChargeSettingsGetAllResponseDto
    toGetAllResponseDto(DriverDeliveryChargeSettings entity) {

        if (entity == null) {
            return null;
        }

        DriverDeliveryChargeSettingsGetAllResponseDto response =
                new DriverDeliveryChargeSettingsGetAllResponseDto();

        response.setDeliveryChargeSettingId(
                entity.getDeliveryChargeSettingId()
        );

        response.setKmsRangeFrom(entity.getKmsRangeFrom());
        response.setKmsRangeTo(entity.getKmsRangeTo());
        response.setUnitPricePerKm(entity.getUnitPricePerKm());

        response.setChargeType(entity.getChargeType());
        response.setDeliveryType(entity.getDeliveryType());
        response.setDriverType(entity.getDriverType());
        response.setServiceType(entity.getServiceType());
        response.setVehicleType(entity.getVehicleType());
        response.setFuelType(entity.getFuelType());

        response.setZoneId(entity.getZoneId());
        response.setCurrencyCode(entity.getCurrencyCode());

        response.setWaitingFreeMinutes(entity.getWaitingFreeMinutes());
        response.setWaitingPerMinute(entity.getWaitingPerMinute());

        response.setNightCharge(entity.getNightCharge());
        response.setPeakCharge(entity.getPeakCharge());
        response.setWeatherSurcharge(entity.getWeatherSurcharge());
        response.setRemoteAreaCharge(entity.getRemoteAreaCharge());
        response.setRemoteZoneSurcharge(entity.getRemoteZoneSurcharge());

        response.setStatus(entity.getStatus());

        return response;
    }
}