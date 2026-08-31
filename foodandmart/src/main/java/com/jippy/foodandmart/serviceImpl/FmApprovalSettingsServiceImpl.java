package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;
import com.jippy.foodandmart.dto.FmUpdateApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmUpdateApprovalSettingsResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalSettings;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmApprovalSettingsMapper;
import com.jippy.foodandmart.repository.FmApprovalSettingsRepository;
import com.jippy.foodandmart.repository.FmManagerAreasRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.IFmApprovalSettingsService;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Implementation for Approval Settings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmApprovalSettingsServiceImpl implements IFmApprovalSettingsService {

    private final FmApprovalSettingsRepository repository;
    private final FmUserRepository userRepository;
    private final FmManagerAreasRepository managerAreasRepository;

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
        // Validate Required Approvals Count
        // For PARALLEL Workflow
        //----------------------------------------------------------

        if (FmAppConstants.WORKFLOW_TYPE_PARALLEL.equalsIgnoreCase(workflowType)) {

            //------------------------------------------------------
            // Count existing active parallel approvers
            //------------------------------------------------------

            long configuredApprovers =
                    repository.countActiveParallelApprovers(
                            entityType,
                            requestDTO.getApprovalLevel());

            //------------------------------------------------------
            // Since current approver is not yet saved,
            // include this approver also.
            //------------------------------------------------------

            configuredApprovers++;

            log.info(
                    "Configured Parallel Approvers : {}, Required Approvals Count : {}",
                    configuredApprovers,
                    requestDTO.getRequiredApprovalsCount());

            //------------------------------------------------------
            // 0 means ALL approvers are required
            //------------------------------------------------------

            if (requestDTO.getRequiredApprovalsCount() != null
                    && requestDTO.getRequiredApprovalsCount() > configuredApprovers) {

                log.warn(
                        "Invalid Required Approvals Count. " +
                                "Configured Approvers : {}, Required Approvals Count : {}",
                        configuredApprovers,
                        requestDTO.getRequiredApprovalsCount());

                throw new IllegalArgumentException(
                        "Required Approvals Count cannot be greater than the" +
                                " total configured Parallel Approvers ("
                                + configuredApprovers + ").");
            }
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

    //==========================================================================
    // replace Approver With Areas
    //==========================================================================

    /**
     * replaceApproverWithAreas
     *
     * <p>
     * Business Rules:
     *
     * 1. Approval Setting must exist.
     * 2. Store the existing Approver Id.
     * 3. Update Approval Setting with the new Approver Id.
     * 4. Update audit information.
     * 5. Save Approval Setting.
     * 6. Update Manager Areas from Old Approver to New Approver.
     *
     * @param requestDTO replaceApproverWithAreas Request
     * @return Updated Approval Settings Response
     */
    @Override
    @Transactional
    public FmUpdateApprovalSettingsResponseDTO replaceApproverWithAreas(
            FmUpdateApprovalSettingsRequestDTO requestDTO) {


        log.info(
                "Started Updating Approval Settings. Approval Settings Id : {}, New Approver Id : {}",
                requestDTO.getApprovalSettingsId(),
                requestDTO.getApproverId());


        //----------------------------------------------------------
        // Fetch Approval Setting
        //----------------------------------------------------------

        FmApprovalSettings approvalSetting =
                repository.findById(requestDTO.getApprovalSettingsId())
                        .orElseThrow(() -> {

                            log.warn(
                                    "Approval Settings not found. Approval Settings Id : {}",
                                    requestDTO.getApprovalSettingsId());

                            return new ResourceNotFoundException(
                                    "Approval Settings not found with Id : "
                                            + requestDTO.getApprovalSettingsId());
                        });

        //----------------------------------------------------------
        // Store Existing Approver Id
        //----------------------------------------------------------

        Integer oldApproverId = approvalSetting.getApproverId();

        if (oldApproverId.equals(requestDTO.getApproverId())) {

            throw new IllegalArgumentException(
                    "New Approver Id must be different from Existing Approver Id.");
        }
        log.info(
                "Existing Approver Id : {}, New Approver Id : {}",
                oldApproverId,
                requestDTO.getApproverId());

        //----------------------------------------------------------
        // Update Approval Settings
        //----------------------------------------------------------

        approvalSetting.setApproverId(requestDTO.getApproverId());

        approvalSetting.setUpdatedAt(LocalDateTime.now());

        approvalSetting.setUpdatedBy(requestDTO.getUpdatedBy());

        //----------------------------------------------------------
        // Save Approval Settings
        //----------------------------------------------------------

        approvalSetting = repository.save(approvalSetting);

        log.info(
                "Approval Settings updated successfully. Approval Settings Id : {}",
                approvalSetting.getApprovalSettingsId());

        //----------------------------------------------------------
        // Update Manager Areas
        //----------------------------------------------------------

        int updatedRows = managerAreasRepository.updateManagerAreas(
                        oldApproverId,
                        requestDTO.getApproverId());

        log.info(
                "{} Manager Area records updated from User {} to User {}",
                updatedRows,
                oldApproverId,
                requestDTO.getApproverId());

        //----------------------------------------------------------
        // Prepare Response
        //----------------------------------------------------------

        FmUpdateApprovalSettingsResponseDTO response =
                FmApprovalSettingsMapper.mapEntityToReplaceApproverResponse(
                                approvalSetting);

        response.setOldApproverId(oldApproverId);
        response.setNewApproverId(requestDTO.getApproverId());
        response.setMessage("Approval Settings updated successfully.");

        //----------------------------------------------------------
        // Return Response
        //----------------------------------------------------------

        return response;
    }

    //==========================================================================
    // getAllSettings
    //==========================================================================

    /**
     * getAllSettings
     *
     * <p>
     * Returns all Approval Settings.
     *
     * @return List of all Approval Settings
     */
    @Override
    public List<FmApprovalSettingsResponseDTO> getAllSettings() {

        log.info("Fetching all Approval Settings.");

        List<FmApprovalSettings> settings = repository.findAll();

        log.info("Found {} Approval Settings.", settings.size());

        return settings.stream()
                .map(FmApprovalSettingsMapper::mapEntityToResponseDto)
                .collect(Collectors.toList());
    }

}