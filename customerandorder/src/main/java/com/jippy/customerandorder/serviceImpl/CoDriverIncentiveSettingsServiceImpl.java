package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoDriverIncentiveSettingsDto;
import com.jippy.customerandorder.entity.CoDriverIncentiveSettings;
import com.jippy.customerandorder.iservice.CoDriverIncentiveSettingsService;
import com.jippy.customerandorder.mapper.CoDriverIncentiveSettingsMapper;
import com.jippy.customerandorder.repository.CoDriverIncentiveSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoDriverIncentiveSettingsServiceImpl implements CoDriverIncentiveSettingsService {

    private final CoDriverIncentiveSettingsRepository incentiveSettingsRepository;

    @Override
    public CoDriverIncentiveSettingsDto saveOrUpdateIncentives(CoDriverIncentiveSettingsDto dto) {

        log.info("Save/Update Incentives START | dto={}", dto);

        // Create new incentive setting if ID is null
        if (dto.getDriverIncentiveSettingsId() == null) {

            log.info("Creating new incentive setting");

//            for post request, ID will be null, so we will create new record
            CoDriverIncentiveSettings entity = CoDriverIncentiveSettingsMapper.toIncentiveEntity(dto);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setCreatedBy(1);

//            save to database and get the saved entity with generated ID
            CoDriverIncentiveSettings saved = incentiveSettingsRepository.save(entity);

            log.info("Created successfully | ID={}", saved.getDriverIncentiveSettingsId());

            // mapper for response entity to DTO
            return CoDriverIncentiveSettingsMapper.incentiveEntityToDto(saved);
        }

        // Update existing incentive setting if ID is provided
        else {

            Integer id = dto.getDriverIncentiveSettingsId();
            log.info("Updating incentive with ID: {}", id);

            CoDriverIncentiveSettings existing = incentiveSettingsRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Record not found for ID: {}", id);
                        return new ResourceNotFoundException("Record not found with ID: " + id);
                    });

//            to update existing record, we will use the mapper method that
//            updates the entity with new values from DTO, without changing the ID
            CoDriverIncentiveSettingsMapper.updateIncentiveEntity(existing, dto);
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setUpdatedBy(1);

//            saving updated entity back to database, which will perform the update operation
            CoDriverIncentiveSettings updated = incentiveSettingsRepository.save(existing);

            log.info("Updated successfully | ID={}", updated.getDriverIncentiveSettingsId());

            // for mapper response entity to DTO
            return CoDriverIncentiveSettingsMapper.incentiveEntityToDto(updated);
        }
    }
}