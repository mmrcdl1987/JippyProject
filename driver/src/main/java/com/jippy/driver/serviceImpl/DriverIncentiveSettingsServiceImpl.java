package com.jippy.driver.serviceImpl;


import com.jippy.driver.dto.DriverIncentiveSettingsDto;
import com.jippy.driver.entity.DriverIncentiveSettings;
import com.jippy.driver.mapper.DriverIncentiveSettingsMapper;
import com.jippy.driver.repositary.DriverIncentiveSettingsRepository;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverIncentiveSettingsServiceImpl implements DriverIncentiveSettingsService {

    private final DriverIncentiveSettingsRepository incentiveSettingsRepository;

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

            DriverIncentiveSettings existing = incentiveSettingsRepository.findById(id)
                    .orElseThrow(() -> {
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
}