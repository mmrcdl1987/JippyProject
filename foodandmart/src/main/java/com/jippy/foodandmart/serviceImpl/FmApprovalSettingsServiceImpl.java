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

    /**
     * Creates a new Approval Setting.
     *
     * <p>
     * Business Rules:
     *
     * 1. Exact duplicate Approval Settings are not allowed.
     *
     * 2. Duplicate combination is:
     *    Entity Type + Approval Level + Approver Id + Workflow Type.
     *
     * 3. Multiple Approvers are allowed for the same
     *    Entity Type and Approval Level.
     *
     * 4. Different Workflow Types are allowed for the same
     *    Entity Type, Approval Level and Approver.
     *
     * 5. Required Approvals Count is mandatory for CASCADE workflow.
     *
     * @param requestDTO Approval Settings Request
     * @return Created Approval Settings
     */
    @Override
    public FmApprovalSettingsResponseDTO createApproval(FmApprovalSettingsRequestDTO requestDTO) {

        log.info(
                "Started Creating Approval Settings. " +
                        "Entity Type : {}, Approval Level : {}, " +
                        "Approver Id : {}, Workflow Type : {}",
                requestDTO.getEntityType(),
                requestDTO.getApprovalLevel(),
                requestDTO.getApproverId(),
                requestDTO.getWorkflowType());

        //----------------------------------------------------------
        // Normalize Values
        //----------------------------------------------------------

        String entityType =
                requestDTO.getEntityType().toUpperCase();

        String workflowType =
                requestDTO.getWorkflowType().toUpperCase();

        //----------------------------------------------------------
        // Check Exact Duplicate Approval Setting
        //----------------------------------------------------------

        boolean approvalSettingExists =
                repository.existsByEntityTypeAndApprovalLevelAndApproverIdAndWorkflowType(
                        entityType,
                        requestDTO.getApprovalLevel(),
                        requestDTO.getApproverId(),
                        workflowType);

        //----------------------------------------------------------
        // Duplicate Approval Setting Found
        //----------------------------------------------------------

        if (approvalSettingExists) {

            log.warn(
                    "Duplicate Approval Settings found. " +
                            "Entity Type : {}, Approval Level : {}, " +
                            "Approver Id : {}, Workflow Type : {}",
                    entityType,
                    requestDTO.getApprovalLevel(),
                    requestDTO.getApproverId(),
                    workflowType);

            throw new DuplicateResourceException(
                    "Approval Settings already exist for Entity Type : "
                            + entityType + ", Approval Level : "
                            + requestDTO.getApprovalLevel() + ", Approver Id : "
                            + requestDTO.getApproverId() + ", Workflow Type : "
                            + workflowType);
        }

        //----------------------------------------------------------
        // Validate CASCADE Workflow
        //----------------------------------------------------------

        /**
         * When Workflow Type is CASCADE,
         * Required Approvals Count must be provided.
         */
        if (FmAppConstants.WORKFLOW_TYPE_CASCADE.equals(workflowType)
                && requestDTO.getRequiredApprovalsCount() == null) {

            log.warn(
                    "Required Approvals Count is missing for Workflow Type : {}",
                    workflowType);

            throw new IllegalArgumentException(
                    "Required Approvals Count is mandatory for Workflow Type : "
                            + workflowType);
        }

        //----------------------------------------------------------
        // Convert Request DTO to Entity
        //----------------------------------------------------------

        FmApprovalSettings entity =
                FmApprovalSettingsMapper.mapRequestDtoToEntity(
                        requestDTO);

        //----------------------------------------------------------
        // Save Approval Setting
        //----------------------------------------------------------

        entity = repository.save(entity);

        log.info(
                "Approval Settings created successfully. " +
                        "Approval Settings Id : {}, Entity Type : {}, " +
                        "Approval Level : {}, Approver Id : {}, Workflow Type : {}",
                entity.getApprovalSettingsId(),
                entity.getEntityType(),
                entity.getApprovalLevel(),
                entity.getApproverId(),
                entity.getWorkflowType());

        //----------------------------------------------------------
        // Convert Entity to Response DTO
        //----------------------------------------------------------

        FmApprovalSettingsResponseDTO response =
                FmApprovalSettingsMapper.mapEntityToResponseDto(
                        entity);

        //----------------------------------------------------------
        // Return Response
        //----------------------------------------------------------

        return response;
    }


}