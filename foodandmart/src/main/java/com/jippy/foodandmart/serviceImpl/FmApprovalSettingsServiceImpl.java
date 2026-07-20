package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalSettings;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmApprovalSettingsMapper;
import com.jippy.foodandmart.repository.FmApprovalSettingsRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.IFmApprovalSettingsService;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for Approval Settings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmApprovalSettingsServiceImpl implements IFmApprovalSettingsService {

    private final FmApprovalSettingsRepository repository;
    private final FmUserRepository userRepository;

    @Override
    public FmApprovalSettingsResponseDTO createApproval(FmApprovalSettingsRequestDTO requestDTO) {

        log.info("Creating Approval Settings for Entity Type : {}", requestDTO.getEntityType());

        /**
         * Check whether an approval configuration already exists
         * for the given Entity Type and Approval Level.
         * This prevents duplicate approval workflows from being created.
         */
        if (repository.existsByEntityTypeAndApprovalLevel(requestDTO.getEntityType(),
                requestDTO.getApprovalLevel())) {

        log.warn("Approval Settings already exist for Entity Type: {} and Approval Level: {}",
                    requestDTO.getEntityType(),
                    requestDTO.getApprovalLevel());

            throw new DuplicateResourceException("Approval Settings already exist for Entity Type: "
                            + requestDTO.getEntityType() + " and Approval Level: "
                            + requestDTO.getApprovalLevel());
        }

        /**
         * Validate the workflow configuration.
         *
         * Business Rule:
         * When the Workflow Type is CASCADE,
         * the Required Approvals Count must be provided
         * because the approval process depends on the
         * configured number of required approvals.
         */
        if (FmAppConstants.WORKFLOW_TYPE_CASCADE.equals(requestDTO.getWorkflowType())
                && requestDTO.getRequiredApprovalsCount() == null) {

            log.warn("Required Approvals Count is missing for Workflow Type: {}",
                    requestDTO.getWorkflowType());

            throw new IllegalArgumentException("Required Approvals Count is mandatory " +
                    "for Workflow Type: " + requestDTO.getWorkflowType());
        }

        /**
         * Validate whether the specified Approver exists.
         * Throw an exception if the provided Approver Id is invalid.
         */
//        if (!userRepository.existsById(requestDTO.getApproverId())) {
//
//            log.warn("Approver not found for Approver Id: {}", requestDTO.getApproverId());
//
//            throw new ResourceNotFoundException("Approver not found with Id: "
//                    + requestDTO.getApproverId());
//        }\
//

        FmApprovalSettings entity = FmApprovalSettingsMapper.mapRequestDtoToEntity(requestDTO);

        entity = repository.save(entity);

        log.info("Approval Settings created successfully with ID : {}", entity.getApprovalSettingsId());

        FmApprovalSettingsResponseDTO response = FmApprovalSettingsMapper.mapEntityToResponseDto(entity);


        return response;

    }

}