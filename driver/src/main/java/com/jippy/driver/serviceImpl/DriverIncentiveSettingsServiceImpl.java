package com.jippy.driver.serviceImpl;


import com.jippy.driver.dto.DriverIncentiveHistoryResponseDto;
import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import com.jippy.driver.entity.DriverIncentiveHistory;
import com.jippy.driver.entity.DriverIncentiveSettings;
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
    public DriverIncentiveSettingsDto saveOrUpdateIncentives(DriverIncentiveSettingsDto dto) {

        log.info("Save/Update Incentives START | dto={}", dto);

        // Create new incentive setting if ID is null
        if (dto.getDriverIncentiveSettingsId() == null) {

            log.info("Creating new incentive setting");

//            for post request, ID will be null, so we will create new record
            DriverIncentiveSettings entity = DriverIncentiveSettingsMapper.toIncentiveEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setCreatedBy(1);

//            save to database and get the saved entity with generated ID
            DriverIncentiveSettings saved = incentiveSettingsRepository.save(entity);

            log.info("Created successfully | ID={}", saved.getDriverIncentiveSettingsId());

            // mapper for response entity to DTO
            return DriverIncentiveSettingsMapper.incentiveEntityToDto(saved);
        }

        // Update existing incentive setting if ID is provided
        else {

            Integer id = dto.getDriverIncentiveSettingsId();
            log.info("Updating incentive with ID: {}", id);

            DriverIncentiveSettings existing = incentiveSettingsRepository.findById(id).orElseThrow(() -> {
                log.error("Record not found for ID: {}", id);
                return new ResourceNotFoundException("Record not found with ID: " + id);
            });

//            to update existing record, we will use the mapper method that
//            updates the entity with new values from DTO, without changing the ID
            DriverIncentiveSettingsMapper.updateIncentiveEntity(existing, dto);
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(1);

//            saving updated entity back to database, which will perform the update operation
            DriverIncentiveSettings updated = incentiveSettingsRepository.save(existing);

            log.info("Updated successfully | ID={}", updated.getDriverIncentiveSettingsId());

            // for mapper response entity to DTO
            return DriverIncentiveSettingsMapper.incentiveEntityToDto(updated);
        }
    }

    @Override
    public Page<DriverIncentiveHistoryResponseDto> getDriverIncentiveHistory
            (Integer driverId, String filter,Integer page,
             Integer size) {

        log.info("Fetching incentive history for driverId : {}, filter : {}", driverId, filter);

        Page<DriverIncentiveHistory> incentiveHistoryList;
// pagination can be applied here if needed, for now we are fetching all records for the month
        Pageable pageable =
                PageRequest.of(
                        page,
                        size);

        if ("currentMonth".equalsIgnoreCase(filter)) {
//            Monthly filter - get records for the current month, we will calculate
//            the start and end date of the current month and fetch records between those dates
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);

            LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            log.info("Fetching current month records between {} and {}", startDate, endDate);


            incentiveHistoryList = repository.findByDriverIdAndCurrDateBetween
                    (driverId, startDate, endDate,pageable);

        } else {

            log.info("Fetching all incentive history records");

            incentiveHistoryList = repository.findByDriverId(driverId,pageable);
        }

        if (incentiveHistoryList.isEmpty()) {

            throw new ResourceNotFoundException("No incentive history found for driverId : " + driverId);
        }

//       converting list of entities to list of DTOs for response
        List<DriverIncentiveHistoryResponseDto> responseList = new ArrayList<>();

        for (DriverIncentiveHistory history : incentiveHistoryList.getContent()) {

            responseList.add(DriverIncentiveSettingsMapper.toResponseDto(history));
        }

        log.info("Total records fetched : {}", responseList.size());
        Page<DriverIncentiveHistoryResponseDto> responsePage =
                new PageImpl<>(
                        responseList,
                        pageable,
                        incentiveHistoryList.getTotalElements());

        log.info("Returning paginated response with {} records",
                responsePage.getNumberOfElements());

        return responsePage;
    }
}