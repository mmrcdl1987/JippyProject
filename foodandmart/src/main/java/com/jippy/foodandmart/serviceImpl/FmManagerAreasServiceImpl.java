package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmManagerAreasRequestDTO;
import com.jippy.foodandmart.dto.FmManagerAreasResponseDTO;
import com.jippy.foodandmart.entity.FmManagerAreas;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmManagerAreasMapper;
import com.jippy.foodandmart.repository.FmAreaRepository;
import com.jippy.foodandmart.repository.FmManagerAreasRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.IFmManagerAreasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service Implementation for assigning
 * Managers to multiple Areas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmManagerAreasServiceImpl implements IFmManagerAreasService {

    /**
     * Repository for Manager Area Mapping.
     */
    private final FmManagerAreasRepository managerAreasRepository;

    /**
     * Repository for User validations.
     */
    private final FmUserRepository userRepository;

    /**
     * Repository for Area validations.
     */
    private final FmAreaRepository areaRepository;

    /**
     * Assigns one Manager to multiple Areas.
     *
     * Business Flow:
     * 1. Validate Manager.
     * 2. Validate duplicate Area Ids.
     * 3. Validate Area Ids.
     * 4. Validate existing mappings.
     * 5. Save mappings.
     * 6. Return response.
     *
     * @param requestDTO Manager Area Request.
     * @return Manager Area Response.
     */
    @Override
    public FmManagerAreasResponseDTO assignManagerAreas(
            FmManagerAreasRequestDTO requestDTO) {

        log.info("Received request to assign {} Area(s) to User Id: {}",
                requestDTO.getAreaIds().size(), requestDTO.getUserId());

//        validateManager(requestDTO.getUserId());

        validateDuplicateAreaIds(requestDTO.getAreaIds());

        validateAreas(requestDTO.getAreaIds());

        validateExistingMappings(requestDTO.getUserId(), requestDTO.getAreaIds());

        // Save mappings
        List<Integer> assignedAreaIds = new ArrayList<>();

        for (Integer areaId : requestDTO.getAreaIds()) {

            FmManagerAreas entity =
                    FmManagerAreasMapper.mapToEntity(requestDTO.getUserId(), areaId);

            managerAreasRepository.save(entity);

            assignedAreaIds.add(areaId);

            log.info("Assigned Area Id: {} to User Id: {}", areaId, requestDTO.getUserId());
        }

        FmManagerAreasResponseDTO response =
                FmManagerAreasMapper.mapToResponseDto(requestDTO.getUserId(), assignedAreaIds);

        log.info("Successfully assigned {} Area(s) to User Id: {}", assignedAreaIds.size(),
                requestDTO.getUserId());

        return response;
    }

//    ---------------------- HELPER METHOD -------------------------------------------
    /**
     * 1. Validates whether the specified Manager exists.
     *
     * @param userId Manager User Id.
     */
//    private void validateManager(Integer userId) {
//
//        if (!userRepository.existsById(userId)) {
//
//            log.warn("Manager not found for User Id: {}", userId);
//
//            throw new ResourceNotFoundException("Manager not found with User Id: " + userId);
//        }
//        log.info("Manager validation successful for User Id: {}", userId);
//    }
// ---------------------------------------------------------------------------------------------------
    /**
     * 2. Validates whether all Area Ids exist.
     *
     * @param areaIds List of Area Ids.
     */
    private void validateAreas(List<Integer> areaIds) {

        for (Integer areaId : areaIds) {

            if (!areaRepository.existsById(areaId)) {

                log.warn("Area not found for Area Id: {}", areaId);

                throw new ResourceNotFoundException("Area not found with Area Id: " + areaId);
            }

            log.info("Area validation successful for Area Id: {}", areaId);
        }

    }
//

    /**
     * Validates whether duplicate Area Ids
     * are present in the request.
     *
     * @param areaIds List of Area Ids.
     */
    private void validateDuplicateAreaIds(List<Integer> areaIds) {

        Set<Integer> uniqueAreaIds = new HashSet<>();

        for (Integer areaId : areaIds) {

            if (!uniqueAreaIds.add(areaId)) {

                log.warn("Duplicate Area Id found in request: {}", areaId);

                throw new IllegalArgumentException("Duplicate Area Id found in request: " + areaId);
            }
        }

        log.info("Duplicate Area Id validation completed successfully.");
    }
//    ----------------------------------------------------------------------------------------------
    /**
     * Validates whether the specified
     * Manager-Area mappings already exist.
     *
     * @param userId Manager User Id.
     * @param areaIds List of Area Ids.
     */
    private void validateExistingMappings(
            Integer userId,
            List<Integer> areaIds) {

        for (Integer areaId : areaIds) {

            if (managerAreasRepository.existsByUserIdAndAreaId(
                    userId,
                    areaId)) {

                log.warn("Area Id: {} is already assigned to User Id: {}", areaId, userId);

                throw new DuplicateResourceException("Area Id " + areaId
                        + " is already assigned to User Id " + userId);
            }
        }

        log.info("Existing Manager-Area mapping validation completed successfully.");
    }

}