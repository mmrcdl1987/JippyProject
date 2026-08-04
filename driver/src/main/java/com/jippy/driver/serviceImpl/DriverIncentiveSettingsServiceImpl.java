package com.jippy.driver.serviceImpl;


import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import com.jippy.driver.entity.DriverIncentiveHistory;
import com.jippy.driver.entity.DriverIncentiveSettings;
import com.jippy.driver.exception.DriverBusinessException;
import com.jippy.driver.mapper.DriverIncentiveSettingsMapper;
import com.jippy.driver.repositary.DriverIncentiveHistoryRepository;
import com.jippy.driver.repositary.DriverIncentiveSettingsRepository;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverIncentiveSettingsServiceImpl implements DriverIncentiveSettingsService {

    private final DriverIncentiveSettingsRepository incentiveSettingsRepository;

    private final DriverIncentiveHistoryRepository repository;

    @Override
    public DriverIncentiveSettingsDto saveOrUpdateIncentives(
            DriverIncentiveSettingsDto dto) {

        log.info("SAVE_OR_UPDATE_INCENTIVE_START | dto={}", dto);

        // ============================================================
        // CREATE NEW INCENTIVE
        // ============================================================
        if (dto.getDriverIncentiveSettingsId() == null) {

            log.info("CREATE_INCENTIVE_START | ordersCount={} | incentiveAmount={}",
                    dto.getOrdersCount(),
                    dto.getIncentiveAmount());
            // --------------------------------------------------------
            // Check duplicate orders count
            // One ordersCount should have only one incentive slab
            // --------------------------------------------------------
            if (incentiveSettingsRepository.existsByOrdersCount(
                    dto.getOrdersCount())) {

                log.error(
                        "Duplicate incentive slab found for ordersCount={}",
                        dto.getOrdersCount());

                throw new DriverBusinessException(
                        "Incentive slab already exists for orders count : "
                                + dto.getOrdersCount());
            }

            // --------------------------------------------------------
            // Map DTO to Entity
            // --------------------------------------------------------
            DriverIncentiveSettings entity =
                    DriverIncentiveSettingsMapper
                            .toIncentiveEntity(dto);

            entity.setCreatedAt(LocalDateTime.now());
            entity.setCreatedBy(1);

            // --------------------------------------------------------
            // Save into database
            // --------------------------------------------------------
            DriverIncentiveSettings saved =
                    incentiveSettingsRepository.save(entity);

            log.info(
                    "Incentive created successfully | id={}",
                    saved.getDriverIncentiveSettingsId());

            DriverIncentiveSettingsDto response =
                    DriverIncentiveSettingsMapper
                            .incentiveEntityToDto(saved);

            log.info(
                    "CREATE_INCENTIVE_SUCCESS | incentiveSettingsId={} | ordersCount={}",
                    response.getDriverIncentiveSettingsId(),
                    response.getOrdersCount());
            return response;
        }

        // ============================================================
        // UPDATE EXISTING INCENTIVE
        // ============================================================
        else {

            Integer incentiveSettingsId = dto.getDriverIncentiveSettingsId();

            log.info(
                    "UPDATE_INCENTIVE_START | incentiveSettingsId={} | ordersCount={} | incentiveAmount={}",
                    incentiveSettingsId,
                    dto.getOrdersCount(),
                    dto.getIncentiveAmount());

            DriverIncentiveSettings existing =
                    incentiveSettingsRepository.findById(incentiveSettingsId)
                            .orElseThrow(() -> {

                                log.error(
                                        "Incentive setting not found | incentiveSettingsId={}",
                                        incentiveSettingsId);

                                return new ResourceNotFoundException(
                                        "Incentive setting not found with ID: "
                                                + incentiveSettingsId);
                            });

                // --------------------------------------------------------
                // Check duplicate orders count
                // Ignore current record while checking duplicates
                // --------------------------------------------------------
            if (incentiveSettingsRepository
                    .existsByOrdersCountAndDriverIncentiveSettingsIdNot(
                            dto.getOrdersCount(),
                            incentiveSettingsId)) {

                log.error(
                        "DUPLICATE_INCENTIVE_SLAB | ordersCount={} | incentiveSettingsId={}",
                        dto.getOrdersCount(),
                        incentiveSettingsId);

                throw new DriverBusinessException(
                        "Duplicate incentive slab found for orders count: "
                                + dto.getOrdersCount());
            }

            // --------------------------------------------------------
            // Update entity values
            // --------------------------------------------------------
            DriverIncentiveSettingsMapper
                    .updateIncentiveEntity(existing, dto);

            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(1);

            // --------------------------------------------------------
            // Save updated record
            // --------------------------------------------------------
            DriverIncentiveSettings updated =
                    incentiveSettingsRepository.save(existing);

            log.info(
                    "Incentive updated successfully | id={}",
                    updated.getDriverIncentiveSettingsId());

            DriverIncentiveSettingsDto response =
                    DriverIncentiveSettingsMapper
                            .incentiveEntityToDto(updated);

            log.info(
                    "UPDATE_INCENTIVE_SUCCESS | incentiveSettingsId={} | ordersCount={}",
                    response.getDriverIncentiveSettingsId(),
                    response.getOrdersCount());

            return response;
        }
    }

    @Override
    public Page<DriverIncentiveHistoryResponseDto> getDriverIncentiveHistory(
            Integer driverId,
            String filter,
            Integer page,
            Integer size) {

        log.info("Fetching incentive history | driverId={} | filter={}",
                driverId, filter);

        // ------------------------------------------------------------
        // Create pagination object
        // ------------------------------------------------------------
        Pageable pageable = PageRequest.of(page, size);

        Page<DriverIncentiveHistory> incentiveHistoryList;

        // ------------------------------------------------------------
        // Fetch current month's incentive history
        // ------------------------------------------------------------
        if (DConstants.CURRENT_MONTH.equalsIgnoreCase(filter)) {

            // First day of current month
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);

            // Last day of current month
            LocalDate endDate = LocalDate.now()
                    .withDayOfMonth(LocalDate.now().lengthOfMonth());

            log.info("Fetching CURRENT_MONTH incentive history | startDate={} | endDate={}",
                    startDate, endDate);

            incentiveHistoryList =
                    repository.findByDriverIdAndCurrDateBetween(
                            driverId,
                            startDate,
                            endDate,
                            pageable);

        }

        // ------------------------------------------------------------
        // Fetch complete incentive history
        // ------------------------------------------------------------
        else if (DConstants.ALL.equalsIgnoreCase(filter)) {

            log.info("Fetching ALL incentive history records");

            incentiveHistoryList =
                    repository.findByDriverId(driverId, pageable);
        }

        // ------------------------------------------------------------
        // Invalid filter value
        // Supported values:
        // CURRENT_MONTH
        // ALL
        // ------------------------------------------------------------
        else {

            log.error("Invalid filter received : {}", filter);

            throw new DriverBusinessException(
                    "Invalid filter. Allowed values are CURRENT_MONTH and ALL.");
        }

        // ------------------------------------------------------------
        // If driver has no incentive history, throw exception.
        // Note:
        // getTotalElements() == 0 means no records exist.
        // If totalElements > 0 and requested page is beyond range,
        // content will be empty but totalElements will still be > 0.
        // This avoids returning 500 for out-of-range pages.
        // ------------------------------------------------------------
        if (incentiveHistoryList.getTotalElements() == 0) {

            log.info("No incentive history found for driverId={}", driverId);

            throw new ResourceNotFoundException(
                    "No incentive history found for driverId : " + driverId);
        }

        // ------------------------------------------------------------
        // Convert Entity list to Response DTO list
        // ------------------------------------------------------------
        List<DriverIncentiveHistoryResponseDto> responseList = new ArrayList<>();

        for (DriverIncentiveHistory history
                : incentiveHistoryList.getContent()) {

            responseList.add(DriverIncentiveSettingsMapper.toResponseDto(history));
        }

        log.info("Total records fetched : {}", responseList.size());

        // ------------------------------------------------------------
        // Prepare paginated response
        // ------------------------------------------------------------
        Page<DriverIncentiveHistoryResponseDto> responsePage =
                new PageImpl<>(
                        responseList,
                        pageable,
                        incentiveHistoryList.getTotalElements());

        log.info(
                "Returning paginated response | page={} | size={} | totalElements={}",
                responsePage.getNumber(),
                responsePage.getNumberOfElements(),
                responsePage.getTotalElements());

        return responsePage;
    }
}