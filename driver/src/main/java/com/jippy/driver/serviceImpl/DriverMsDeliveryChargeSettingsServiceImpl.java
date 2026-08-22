package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.DriverDeliveryChargeSettingsDeleteRequestDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetAllResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsGetByIdResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsPaginationResponseDto;
import com.jippy.driver.dto.DriverDeliveryChargeSettingsSaveRequestDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import com.jippy.driver.mapper.DriverMsDeliveryChargeSettingsMapper;
import com.jippy.driver.repositary.DriverDeliveryChargeSettingsRepository;
import com.jippy.driver.service.DriverMsDeliveryChargeSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DriverMsDeliveryChargeSettingsServiceImpl implements DriverMsDeliveryChargeSettingsService {

    private final DriverDeliveryChargeSettingsRepository repository;

    private final DriverMsDeliveryChargeSettingsMapper mapper;


    // ============================================================
    // SAVE
    // CREATE + UPDATE
    // ============================================================

    @Override
    public DriverDeliveryChargeSettingsGetByIdResponseDto save(DriverDeliveryChargeSettingsSaveRequestDto request) {

        log.info("Started saving driver delivery charge settings");

        validateSaveRequest(request);

        DriverDeliveryChargeSettings entity;

        // ========================================================
        // CREATE
        // ========================================================

        if (request.getDeliveryChargeSettingId() == null) {

            log.info("Creating new driver delivery charge setting. createdBy={}", request.getCreatedBy());

            entity = mapper.toEntity(request);

            LocalDateTime currentDateTime = LocalDateTime.now();

            entity.setCreatedAt(currentDateTime);
            entity.setCreatedBy(request.getCreatedBy());

            entity.setUpdatedAt(null);
            entity.setUpdatedBy(null);

            entity = repository.save(entity);

            log.info("Driver delivery charge setting created successfully. id={}, createdBy={}", entity.getDeliveryChargeSettingId(), entity.getCreatedBy());
        }

        // ========================================================
        // UPDATE
        // ========================================================

        else {

            Integer settingId = request.getDeliveryChargeSettingId();

            log.info("Updating driver delivery charge setting. id={}, updatedBy={}", settingId, request.getUpdatedBy());

            entity = repository.findById(settingId).orElseThrow(() -> {
                log.warn("Driver delivery charge setting not found for update. id={}", settingId);

                return new IllegalArgumentException("Driver delivery charge setting not found with ID: " + settingId);
            });

            // ----------------------------------------------------
            // Preserve original audit information
            // ----------------------------------------------------

            mapper.updateEntity(entity, request);

            entity.setUpdatedAt(LocalDateTime.now());
            entity.setUpdatedBy(request.getUpdatedBy());

            entity = repository.save(entity);

            log.info("Driver delivery charge setting updated successfully. id={}, updatedBy={}", entity.getDeliveryChargeSettingId(), entity.getUpdatedBy());
        }

        log.info("Completed saving driver delivery charge settings. id={}", entity.getDeliveryChargeSettingId());

        return mapper.toGetByIdResponseDto(entity);
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public DriverDeliveryChargeSettingsGetByIdResponseDto getById(Integer deliveryChargeSettingId) {

        log.info("Fetching driver delivery charge setting. id={}", deliveryChargeSettingId);

        if (deliveryChargeSettingId == null || deliveryChargeSettingId <= 0) {

            log.warn("Invalid delivery charge setting ID received. id={}", deliveryChargeSettingId);

            throw new IllegalArgumentException("Delivery charge setting ID must be greater than 0");
        }

        DriverDeliveryChargeSettings entity = repository.findById(deliveryChargeSettingId).orElseThrow(() -> {

            log.warn("Driver delivery charge setting not found. id={}", deliveryChargeSettingId);

            return new IllegalArgumentException("Driver delivery charge setting not found with ID: " + deliveryChargeSettingId);
        });

        log.info("Driver delivery charge setting fetched successfully. id={}", deliveryChargeSettingId);

        return mapper.toGetByIdResponseDto(entity);
    }


    // ============================================================
    // GET ALL WITH PAGINATION
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public DriverDeliveryChargeSettingsPaginationResponseDto getAll(int page, int size) {

        log.info("Fetching driver delivery charge settings. page={}, size={}", page, size);

        // ========================================================
        // Pagination validation
        // ========================================================

        if (page < 0) {

            log.warn("Invalid pagination page received. page={}", page);

            throw new IllegalArgumentException("Page number cannot be less than 0");
        }

        if (size <= 0) {

            log.warn("Invalid pagination size received. size={}", size);

            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        // Prevent extremely large page sizes
        if (size > 100) {

            log.warn("Pagination size exceeds maximum allowed size. size={}", size);

            throw new IllegalArgumentException("Page size cannot be greater than 100");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<DriverDeliveryChargeSettings> entityPage = repository.findAll(pageable);

        List<DriverDeliveryChargeSettingsGetAllResponseDto> content = entityPage.getContent().stream().map(mapper::toGetAllResponseDto).toList();

        log.info("Driver delivery charge settings fetched successfully. page={}, size={}, totalElements={}", entityPage.getNumber(), entityPage.getSize(), entityPage.getTotalElements());

        return new DriverDeliveryChargeSettingsPaginationResponseDto(content, entityPage.getNumber(), entityPage.getSize(), entityPage.getTotalElements(), entityPage.getTotalPages());
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public void delete(DriverDeliveryChargeSettingsDeleteRequestDto request) {

        log.info("Started deleting driver delivery charge setting");

        if (request == null || request.getDeliveryChargeSettingId() == null || request.getDeliveryChargeSettingId() <= 0) {

            log.warn("Invalid delete request received");

            throw new IllegalArgumentException("Valid delivery charge setting ID is required");
        }

        Integer id = request.getDeliveryChargeSettingId();

        DriverDeliveryChargeSettings entity = repository.findById(id).orElseThrow(() -> {

            log.warn("Driver delivery charge setting not found for deletion. id={}", id);

            return new IllegalArgumentException("Driver delivery charge setting not found with ID: " + id);
        });

        repository.delete(entity);

        log.info("Driver delivery charge setting deleted successfully. id={}", id);
    }


    // ============================================================
    // SAVE REQUEST VALIDATION
    // ============================================================

    private void validateSaveRequest(DriverDeliveryChargeSettingsSaveRequestDto request) {

        // ========================================================
        // Null request
        // ========================================================

        if (request == null) {

            log.warn("Null driver delivery charge settings request received");

            throw new IllegalArgumentException("Delivery charge settings request cannot be null");
        }


        // ========================================================
        // Determine CREATE / UPDATE
        // ========================================================

        boolean isCreate = request.getDeliveryChargeSettingId() == null;


        // ========================================================
        // Created By / Updated By
        // ========================================================

        if (isCreate) {

            if (request.getCreatedBy() == null || request.getCreatedBy() <= 0) {

                throw new IllegalArgumentException("Created by user ID is required for creating delivery charge settings");
            }

        } else {

            if (request.getUpdatedBy() == null || request.getUpdatedBy() <= 0) {

                throw new IllegalArgumentException("Updated by user ID is required for updating delivery charge settings");
            }
        }


        // ========================================================
        // KM RANGE FROM
        // ========================================================

        validateNonNegative(request.getKmsRangeFrom(), "KMS range from");


        // ========================================================
        // KM RANGE TO
        // ========================================================

        validateNonNegative(request.getKmsRangeTo(), "KMS range to");


        // ========================================================
        // KM RANGE LOGIC
        // ========================================================

        if (request.getKmsRangeFrom() != null && request.getKmsRangeTo() != null && request.getKmsRangeFrom().compareTo(request.getKmsRangeTo()) >= 0) {

            throw new IllegalArgumentException("KMS range from must be less than KMS range to");
        }


        // ========================================================
        // UNIT PRICE
        // ========================================================

        validateNonNegative(request.getUnitPricePerKm(), "Unit price per KM");


        // ========================================================
        // REQUIRED STRING FIELDS
        // ========================================================

        validateRequiredString(request.getChargeType(), "Charge type");

        validateRequiredString(request.getDeliveryType(), "Delivery type");

        validateRequiredString(request.getDriverType(), "Driver type");

        validateRequiredString(request.getServiceType(), "Service type");

        validateRequiredString(request.getVehicleType(), "Vehicle type");

        validateRequiredString(request.getFuelType(), "Fuel type");

        validateRequiredString(request.getCurrencyCode(), "Currency code");

        validateRequiredString(request.getStatus(), "Status");


        // ========================================================
        // ZONE
        // ========================================================

        if (request.getZoneId() == null || request.getZoneId() <= 0) {

            throw new IllegalArgumentException("Valid zone ID is required");
        }


        // ========================================================
        // WAITING FREE MINUTES
        // ========================================================

        if (request.getWaitingFreeMinutes() == null || request.getWaitingFreeMinutes() < 0) {

            throw new IllegalArgumentException("Waiting free minutes cannot be negative");
        }


        // ========================================================
        // WAITING PER MINUTE
        // ========================================================

        validateNonNegative(request.getWaitingPerMinute(), "Waiting charge per minute");


        // ========================================================
        // NIGHT CHARGE
        // ========================================================

        validateNonNegative(request.getNightCharge(), "Night charge");


        // ========================================================
        // PEAK CHARGE
        // ========================================================

        validateNonNegative(request.getPeakCharge(), "Peak charge");


        // ========================================================
        // WEATHER SURCHARGE
        // ========================================================

        validateNonNegative(request.getWeatherSurcharge(), "Weather surcharge");


        // ========================================================
        // REMOTE AREA CHARGE
        // ========================================================

        validateNonNegative(request.getRemoteAreaCharge(), "Remote area charge");


        // ========================================================
        // REMOTE ZONE SURCHARGE
        // ========================================================

        validateNonNegative(request.getRemoteZoneSurcharge(), "Remote zone surcharge");
    }


    // ============================================================
    // NON-NEGATIVE DECIMAL VALIDATION
    // ============================================================

    private void validateNonNegative(BigDecimal value, String fieldName) {

        if (value == null) {

            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }


    // ============================================================
    // REQUIRED STRING VALIDATION
    // ============================================================

    private void validateRequiredString(String value, String fieldName) {

        if (value == null || value.trim().isEmpty()) {

            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}