package com.jippy.driver.serviceImpl;


import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsResponseDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import com.jippy.driver.exception.CartException;
import com.jippy.driver.mapper.DriverDeliveryChargeSettingsMapper;
import com.jippy.driver.repositary.DriverDeliveryChargeSettingsRepository;
import com.jippy.driver.service.DriverDeliveryChargeSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverDeliveryChargeSettingsServiceImpl
        implements DriverDeliveryChargeSettingsService {

    private final DriverDeliveryChargeSettingsRepository repository;
    private final DriverDeliveryChargeSettingsMapper mapper;

    @Override
    public DriverDeliveryChargeSettingsResponseDto createDriverDeliveryChargeSetting(
            DriverDeliveryChargeSettingsRequestDto requestDto) {

        log.info("SERVICE START: Create driver delivery charge setting | pickUpKmsFrom={}, pickUpKmsTo={}, deliveryKmsFrom={}, deliveryKmsTo={}",
                requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo(),
                requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        // VALIDATE INPUT - NULL CHECK
        if (requestDto == null) {
            log.error("Request DTO is null");
            throw new CartException("Invalid request data");
        }

        // VALIDATE PICKUP KMS RANGE
        if (requestDto.getPickUpKmsRangeFrom() == null || requestDto.getPickUpKmsRangeTo() == null) {
            log.warn("Invalid pickup KMS range | from={}, to={}", 
                    requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo());
            throw new CartException("Pickup KMS range cannot be null");
        }

        if (requestDto.getPickUpKmsRangeFrom().compareTo(requestDto.getPickUpKmsRangeTo()) >= 0) {
            log.warn("Invalid pickup KMS range | from={}, to={}", 
                    requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo());
            throw new CartException(DConstants.MSG_INVALID_KMS_RANGE);
        }

        log.debug("Pickup KMS range validation passed | from={}, to={}", 
                requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo());

        // VALIDATE DELIVERY KMS RANGE
        if (requestDto.getDeliveryKmsRangeFrom() == null || requestDto.getDeliveryKmsRangeTo() == null) {
            log.warn("Invalid delivery KMS range | from={}, to={}", 
                    requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());
            throw new CartException("Delivery KMS range cannot be null");
        }

        if (requestDto.getDeliveryKmsRangeFrom().compareTo(requestDto.getDeliveryKmsRangeTo()) >= 0) {
            log.warn("Invalid delivery KMS range | from={}, to={}", 
                    requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());
            throw new CartException(DConstants.MSG_INVALID_KMS_RANGE);
        }

        log.debug("Delivery KMS range validation passed | from={}, to={}", 
                requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        // VALIDATE UNIT PRICES
        if (requestDto.getUnitPricePerPickKm() == null || requestDto.getUnitPricePerDeliverKm() == null) {
            log.warn("Unit prices are null");
            throw new CartException("Unit prices cannot be null");
        }

        if (requestDto.getUnitPricePerPickKm().compareTo(BigDecimal.ZERO) < 0 || 
            requestDto.getUnitPricePerDeliverKm().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid unit prices | pickPrice={}, deliveryPrice={}", 
                    requestDto.getUnitPricePerPickKm(), requestDto.getUnitPricePerDeliverKm());
            throw new CartException(DConstants.MSG_INVALID_UNIT_PRICE);
        }

        log.debug("Unit prices validation passed | pickPrice={}, deliveryPrice={}", 
                requestDto.getUnitPricePerPickKm(), requestDto.getUnitPricePerDeliverKm());

        // MAP DTO TO ENTITY
        log.debug("Mapping request DTO to entity");

        DriverDeliveryChargeSettings entity = mapper.mapToEntity(requestDto);

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(1);

        log.debug("Entity mapping completed");

        // SAVE TO DATABASE
        DriverDeliveryChargeSettings savedEntity;

        try {
            log.debug("Saving driver delivery charge settings to database");
            savedEntity = repository.save(entity);
            log.debug("Entity saved successfully | id={}", savedEntity.getDeliveryChargeSettingId());
        } catch (Exception ex) {
            log.error("Database error while saving delivery charge settings | error={}", 
                    ex.getMessage(), ex);
            throw new CartException(DConstants.MSG_DATABASE_ERROR);
        }

        // MAP ENTITY TO RESPONSE DTO
        log.debug("Mapping saved entity to response DTO");

        DriverDeliveryChargeSettingsResponseDto responseDto = mapper.mapToResponseDto(savedEntity);

        log.info("SERVICE END: Driver delivery charge setting created successfully | id={}, pickUpRange={}-{}, deliveryRange={}-{}",
                responseDto.getDeliveryChargeSettingId(),
                requestDto.getPickUpKmsRangeFrom(), requestDto.getPickUpKmsRangeTo(),
                requestDto.getDeliveryKmsRangeFrom(), requestDto.getDeliveryKmsRangeTo());

        return responseDto;
    }
}