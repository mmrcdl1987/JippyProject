package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.entity.FmApprovalSettings;
import com.jippy.foodandmart.entity.FmManagerAreas;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.mapper.FmApprovalRequestMapper;
import com.jippy.foodandmart.projections.*;
import com.jippy.foodandmart.repository.FmApprovalRequestRepository;
import com.jippy.foodandmart.repository.FmApprovalSettingsRepository;
import com.jippy.foodandmart.repository.FmManagerAreasRepository;
import com.jippy.foodandmart.repository.FmOutletAddressRepository;
import com.jippy.foodandmart.service.IFmApprovalRequestService;
import jakarta.persistence.criteria.From;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for Approval Requests.
 * <p>
 * Responsible for creating Approval Requests
 * whenever a new Entity enters the Approval Workflow.
 */
//--------------------------------------------------------------------------------------------------

/**
 * ===========================================================
 * Approval Request Service Implementation
 * ===========================================================
 * <p>
 * This service is responsible for fetching all
 * Level-1 Pending Approval Requests assigned
 * to an Approver.
 * <p>
 * Flow
 * -----
 * <p>
 * 1. Validate Approver.
 * <p>
 * 2. Fetch Active Approval Settings.
 * <p>
 * 3. Validate Manager Area Mapping.
 * <p>
 * 4. Fetch Pending Approval Requests.
 * <p>
 * 5. Based on Entity Type
 * <p>
 * OUTLET
 * MERCHANT
 * DRIVER
 * <p>
 * fetch complete entity details.
 * <p>
 * ===========================================================
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class FmApprovalRequestServiceImpl implements IFmApprovalRequestService {

    private final DriverFeignClient driverFeignClient;

    private FmOutletAddressRepository outletAddressRepository;
    /**
     * Repository for Approval Settings.
     */
    private final FmApprovalSettingsRepository approvalSettingsRepository;

    /**
     * Repository for Manager Areas.
     */
    private final FmManagerAreasRepository managerAreasRepository;

    /**
     * Repository for Approval Requests.
     */
    private final FmApprovalRequestRepository approvalRequestRepository;

    /**/

    /**
     * Creates a new Approval Request.
     * <p>
     * Business Flow:
     * 1. Convert input parameters into Approval Request Entity.
     * 2. Set Current Level as Level 1.
     * 3. Set Status as PENDING.
     * 4. Save the Approval Request.
     *
     * @param entityType MERCHANT / OUTLET / DRIVER
     * @param entityId   Merchant Id / Outlet Id / Driver Id
     * @param createdBy  Logged-in User Id
     */
    @Override
    public void createApprovalRequest(String entityType, Integer entityId, Integer createdBy) {

        log.info("Creating Approval Request for Entity Type: {}, Entity Id: {}", entityType, entityId);

        /**
         * Convert the given details into
         * Approval Request Entity.
         */
        FmApprovalRequest approvalRequest =
                FmApprovalRequestMapper.toEntity(entityType, entityId, createdBy);

        /**
         * Save Approval Request.
         */
        approvalRequestRepository.save(approvalRequest);

        log.info("Approval Request created successfully for Entity Type: {}," + " Entity Id: {}", entityType, entityId);
    }
//    ------------------------------------------------------------------------------------
    /**
     * Creates Approval Request from Request DTO.
     *
     * Used by external microservices through Feign Client.
     *
     * @param requestDTO Approval Request DTO.
     */
//    ------------------------------------------------------------------------------
//    From Driver Feign

    /**
     * Creates Approval Request from Request DTO.
     * <p>
     * Used by external microservices through Feign Client.
     *
     * @param requestDTO Approval Request DTO.
     */
    @Override
    public void createApprovalRequest(FmApprovalRequestDTO requestDTO) {

        createApprovalRequest(requestDTO.getEntityType(),
                requestDTO.getEntityId(), requestDTO.getCreatedBy());
    }
//    -----------------------------------------------------------------------


    /**
     * ===========================================================
     * Fetch Level-1 Pending Approval Requests
     * ===========================================================
     *
     * @param approverId Approver User Id
     * @return List of Pending Approval Requests
     */
    @Override
    public List<FmLevel1PendingApprovalResponseDTO> getLevel1PendingApprovalRequests(Integer approverId) {

        log.info("Fetching Level-1 Pending Approval Requests for Approver Id : {}", approverId);

        validateApproverId(approverId);

        List<FmApprovalSettings> approvalSettingsList = getApprovalSettings(approverId);

        validateManagerAreas(approverId);

        List<FmLevel1PendingApprovalResponseDTO> responseList = new ArrayList<>();

        for (FmApprovalSettings approvalSettings : approvalSettingsList) {

            processApprovalRequest(approvalSettings, responseList);
        }

        log.info("Successfully fetched {} Pending Approval Requests.", responseList.size());

        return responseList;
    }

    /**
     * Changes an Approval Request from REJECTED status
     * back to PENDING status.
     *
     * <p>
     * Processing Flow:
     *
     * 1. Find Approval Request using Approval Request Id.
     * 2. Throw ResourceNotFoundException if request does not exist.
     * 3. Validate that current status is REJECTED.
     * 4. Reject the operation when status is not REJECTED.
     * 5. Change status from REJECTED to PENDING.
     * 6. Keep Entity Type, Entity Id and Current Level unchanged.
     * 7. Update audit information.
     * 8. Save the Approval Request.
     * 9. Return updated Approval Request information.
     *
     * @param requestDTO Request containing Approval Request Id and Updated By
     * @return Updated Approval Request details
     */
    @Override
    @Transactional
    public FmRejectedApprovalToPendingResponseDTO updateRejectedApprovalsToPending(
            FmRejectedApprovalToPendingRequestDTO requestDTO) {

        log.info(
                "Started updating Rejected Approval Request to PENDING. " +
                        "Approval Request Id : {}, Updated By : {}",
                requestDTO.getApprovalRequestId(),
                requestDTO.getUpdatedBy());

        //----------------------------------------------------------
        // Step 1: Fetch Approval Request
        //----------------------------------------------------------

        FmApprovalRequest approvalRequest =
                approvalRequestRepository
                        .findById(requestDTO.getApprovalRequestId())
                        .orElseThrow(() -> {

                            log.warn(
                                    "Approval Request not found. Approval Request Id : {}",
                                    requestDTO.getApprovalRequestId());

                            return new ResourceNotFoundException(
                                    "Approval Request not found with Id : "
                                            + requestDTO.getApprovalRequestId());
                        });

        log.debug(
                "Approval Request found. Id : {}, Entity Type : {}, " +
                        "Entity Id : {}, Current Level : {}, Status : {}",
                approvalRequest.getApprovalRequestId(),
                approvalRequest.getEntityType(),
                approvalRequest.getEntityId(),
                approvalRequest.getCurrentLevel(),
                approvalRequest.getStatus());

        //----------------------------------------------------------
        // Step 2: Validate Current Status
        //
        // Business Rule:
        // Only REJECTED Approval Requests are allowed to
        // move back to PENDING.
        //----------------------------------------------------------

        if (!FmAppConstants.APPROVAL_STATUS_REJECTED
                .equalsIgnoreCase(approvalRequest.getStatus())) {

            log.warn(
                    "Approval Request cannot be changed to PENDING because " +
                            "current status is not REJECTED. " +
                            "Approval Request Id : {}, Current Status : {}",
                    approvalRequest.getApprovalRequestId(),
                    approvalRequest.getStatus());

            throw new IllegalArgumentException(
                    "Only REJECTED Approval Requests can be updated to PENDING. "
                            + "Approval Request Id : "
                            + approvalRequest.getApprovalRequestId()
                            + ", Current Status : "
                            + approvalRequest.getStatus());
        }

        //----------------------------------------------------------
        // Step 3: Change REJECTED → PENDING
        //
        // IMPORTANT:
        // Current Level must NOT be changed.
        // Entity Type must NOT be changed.
        // Entity Id must NOT be changed.
        //----------------------------------------------------------

        approvalRequest.setStatus(FmAppConstants.APPROVAL_STATUS_PENDING);

        //----------------------------------------------------------
        // Step 4: Update Audit Information
        //----------------------------------------------------------

        approvalRequest.setUpdatedAt(LocalDateTime.now());

        approvalRequest.setUpdatedBy(requestDTO.getUpdatedBy());

        //----------------------------------------------------------
        // Step 5: Save Updated Approval Request
        //----------------------------------------------------------

        approvalRequest = approvalRequestRepository.save(approvalRequest);

        log.info(
                "Rejected Approval Request successfully updated to PENDING. " +
                        "Approval Request Id : {}, Entity Type : {}, " +
                        "Entity Id : {}, Current Level : {}, Status : {}, " +
                        "Updated By : {}",
                approvalRequest.getApprovalRequestId(),
                approvalRequest.getEntityType(),
                approvalRequest.getEntityId(),
                approvalRequest.getCurrentLevel(),
                approvalRequest.getStatus(),
                approvalRequest.getUpdatedBy());

        //----------------------------------------------------------
        // Step 6: Convert Entity → Response DTO
        //----------------------------------------------------------

        FmRejectedApprovalToPendingResponseDTO response =
                FmApprovalRequestMapper.mapToRejectedApprovalToPendingResponse(
                                approvalRequest);

        //----------------------------------------------------------
        // Step 7: Return Response
        //----------------------------------------------------------

        return response;
    }

 //--------------------------------------------------------------------------------------------
//-------------------------------HELPER METHODS -----------------------------------------------
//--------------------------------------------------------------------------------------------

// 1. HELPER METHOD
    private void validateApproverId(Integer approverId) {

        log.info("Validating Approver Id : {}", approverId);
        if (approverId == null || approverId <= 0) {

            log.error("Invalid Approver Id : {}", approverId);
            throw new IllegalArgumentException("Approver Id must be greater than zero.");
        }
        log.info("Approver Id validation completed successfully.");
    }

// 2. HELPER METHOD
    private List<FmApprovalSettings> getApprovalSettings(Integer approverId) {

        log.info("Fetching Approval Settings for Approver Id : {}", approverId);

        List<FmApprovalSettings> approvalSettingsList =
                approvalSettingsRepository.findByApproverIdAndIsActiveTrue(approverId);

        if (approvalSettingsList.isEmpty()) {

            log.error("Approval Settings not found for Approver Id : {}", approverId);

            throw new ResourceNotFoundException("Approval Settings not found for Approver Id : "
                    + approverId);
        }

        log.info("Total Approval Settings Found : {}", approvalSettingsList.size());

        return approvalSettingsList;
    }


// 3. HELPER METHOD
    private void validateManagerAreas(Integer approverId) {

        log.info("Fetching Manager Areas for Approver Id : {}", approverId);

        List<FmManagerAreas> managerAreas = managerAreasRepository.findByUserId(approverId);

        if (managerAreas.isEmpty()) {

            log.error("No Manager Areas Assigned for Approver Id : {}", approverId);

            throw new ResourceNotFoundException("No Areas Assigned for Approver Id : " + approverId);
        }

        log.info("Total Manager Areas Found : {}", managerAreas.size());
    }


/*
// 4. HELPER METHOD
*/
    private void processApprovalRequest(FmApprovalSettings approvalSettings,
                                        List<FmLevel1PendingApprovalResponseDTO> responseList) {

//        pre-storing the values for Comparision
        String entityType = approvalSettings.getEntityType();
        String approvalLevel = approvalSettings.getApprovalLevel();

        log.info("Processing Entity Type : {}, Approval Level : {}", entityType, approvalLevel);

//        if (!FmAppConstants.APPROVAL_LEVEL_1.equalsIgnoreCase(approvalLevel)) {
//
//            log.info("Skipping Unsupported Approval Level : {}", approvalLevel);
//
//            return;
//        }

        switch (entityType) {

            case FmAppConstants.TYPE_OUTLET:
                processOutletRequests(approvalSettings, responseList);
                break;

            case FmAppConstants.TYPE_MERCHANT:
                processMerchantRequests(approvalSettings, responseList);
                break;

            case FmAppConstants.TYPE_DRIVER:
                processDriverRequests(approvalSettings, responseList);
                break;

            default:
                log.warn("Unsupported Entity Type : {}", entityType);
        }
    }


    /** 5. HELPER METHOD
     * Processes Pending OUTLET Approval Requests
     * assigned to the current Approver.
     *
     * <p>
     * Business Rules:
     *
     * 1. Request must be PENDING.
     * 2. Request current level must match the configured Approval Level.
     * 3. Request Entity Type must be OUTLET.
     * 4. The logged-in Approver must not have already approved
     *    the same Outlet at the same Approval Level.
     * 5. Approval by another parallel Approver must not hide
     *    the request from the current Approver.
     *
     * @param approvalSettings Approval configuration of the current Approver
     * @param responseList     Final Pending Approval Response List
     */
    private void processOutletRequests(
            FmApprovalSettings approvalSettings,
            List<FmLevel1PendingApprovalResponseDTO> responseList) {

        //----------------------------------------------------------
        // Extract Approval Configuration
        //----------------------------------------------------------

        String entityType = approvalSettings.getEntityType();

        String approvalLevel = approvalSettings.getApprovalLevel();

        Integer approverId = approvalSettings.getApproverId();

        String workflowType = approvalSettings.getWorkflowType();

        log.info("Processing Pending OUTLET Approval Requests. " +
                        "Entity Type : {}, Approval Level : {}, " +
                        "Approver Id : {}, Workflow Type : {}",
                entityType,
                approvalLevel,
                approverId,
                workflowType);

        //----------------------------------------------------------
        // Fetch Pending OUTLET Requests
        //
        // Repository excludes requests already APPROVED
        // by this same Approver at this same Level.
        //----------------------------------------------------------

        List<FmOutletLevel1PendingApprovalProjection> outletList =
                approvalRequestRepository.getOutletLevel1PendingRequests(
                                approvalLevel,
                                approverId);

        //----------------------------------------------------------
        // No Pending Requests Found
        //----------------------------------------------------------

        if (outletList.isEmpty()) {

            log.info("No Pending OUTLET Approval Requests found " + "for Approver Id : {}",
                    approverId);

            return;
        }

        //----------------------------------------------------------
        // Convert Projection to Response DTO
        //----------------------------------------------------------

        for (FmOutletLevel1PendingApprovalProjection outlet : outletList) {

            responseList.add(FmApprovalRequestMapper.toOutletResponse(outlet));
        }

        log.info("Completed Processing OUTLET Approval Requests. " +
                        "Approver Id : {}, Total Requests : {}", approverId, outletList.size());
    }


    /**  6. HELPER METHOD
     * Processes Pending MERCHANT Approval Requests
     * for the configured Approver.
     *
     * <p>
     * Business Rules:
     *
     * 1. Request must be in PENDING status.
     * 2. Current Level must match the configured Approval Level.
     * 3. If the current Approver already approved the same
     *    Merchant at the same Approval Level, the request
     *    must not be shown again to that Approver.
     * 4. Approval by another parallel Approver must not
     *    hide the request from the current Approver.
     *
     * @param approvalSettings Approval configuration of the current Approver
     * @param responseList     Final Pending Approval Response List
     */
    private void processMerchantRequests(
            FmApprovalSettings approvalSettings,
            List<FmLevel1PendingApprovalResponseDTO> responseList) {

        //----------------------------------------------------------
        // Extract Approval Configuration
        //----------------------------------------------------------

        String approvalLevel =
                approvalSettings.getApprovalLevel();

        Integer approverId =
                approvalSettings.getApproverId();

        log.info("Fetching Pending MERCHANT Approval Requests. " +
                        "Approval Level : {}, Approver Id : {}",
                approvalLevel,
                approverId);

        //----------------------------------------------------------
        // Fetch Pending MERCHANT Approval Requests
        //
        // Requests already APPROVED by this same Approver
        // for the same Merchant and Level will be excluded
        // by the repository query.
        //----------------------------------------------------------

        List<FmMerchantLevel1PendingApprovalProjection> merchantList =
                approvalRequestRepository.getMerchantLevel1PendingRequests(
                        approvalLevel,
                        approverId);

        //----------------------------------------------------------
        // No Pending Requests Found
        //----------------------------------------------------------

        if (merchantList.isEmpty()) {

            log.info(
                    "No Pending MERCHANT Approval Requests Found. " +
                            "Approver Id : {}",
                    approverId);

            return;
        }

        log.info(
                "Total Pending MERCHANT Requests : {} for Approver Id : {}",
                merchantList.size(),
                approverId);

        //----------------------------------------------------------
        // Convert Projection to Response DTO
        //----------------------------------------------------------

        for (FmMerchantLevel1PendingApprovalProjection merchant : merchantList) {

            responseList.add(
                    FmApprovalRequestMapper.toMerchantResponse(merchant));
        }

        //----------------------------------------------------------
        // Processing Completed
        //----------------------------------------------------------

        log.info(
                "Completed Processing MERCHANT Approval Requests. " +
                        "Approver Id : {}",
                approverId);
    }

    /** 7. HELPER METHOD
     * Processes Pending DRIVER Approval Requests
     * for the configured Approver.
     *
     * <p>
     * Business Rules:
     *
     * 1. Request must be in PENDING status.
     * 2. Current Level must match the configured Approval Level.
     * 3. If the current Approver already approved the same
     *    Driver at the same Approval Level, the request
     *    must not be shown again to that Approver.
     * 4. Approval by another parallel Approver must not
     *    hide the request from the current Approver.
     *
     * @param approvalSettings Approval configuration of the current Approver
     * @param responseList     Final Pending Approval Response List
     */
    private void processDriverRequests(
            FmApprovalSettings approvalSettings,
            List<FmLevel1PendingApprovalResponseDTO> responseList) {

        //----------------------------------------------------------
        // Extract Approval Configuration
        //----------------------------------------------------------

        String approvalLevel =
                approvalSettings.getApprovalLevel();

        Integer approverId =
                approvalSettings.getApproverId();

        log.info(
                "Fetching Pending DRIVER Approval Requests. " +
                        "Approval Level : {}, Approver Id : {}",
                approvalLevel,
                approverId);

        //----------------------------------------------------------
        // Fetch Pending DRIVER Approval Requests
        //
        // Requests already APPROVED by this same Approver
        // for the same Driver and Level will be excluded
        // by the repository query.
        //----------------------------------------------------------

        List<FmDriverLevel1PendingApprovalProjection> driverRequests =
                approvalRequestRepository.getDriverLevel1PendingRequests(
                        approvalLevel,
                        approverId);

        //----------------------------------------------------------
        // No Pending Requests Found
        //----------------------------------------------------------

        if (driverRequests.isEmpty()) {

            log.info("No Pending DRIVER Approval Requests Found. " + "Approver Id : {}",
                    approverId);

            return;
        }

        log.info("Total Pending DRIVER Requests : {} for Approver Id : {}",
                driverRequests.size(),
                approverId);

        //----------------------------------------------------------
        // Process each Pending DRIVER Request
        //----------------------------------------------------------

        for (FmDriverLevel1PendingApprovalProjection projection : driverRequests) {

            //------------------------------------------------------
            // Fetch Driver Details from Driver Microservice
            //------------------------------------------------------

            FmDriverApprovalResponseDTO driverResponse =
                    driverFeignClient.getDriverById(
                            projection.getDriverId());

            //------------------------------------------------------
            // Fetch Driver Address from FM Database
            //------------------------------------------------------

            FmDriverAddressProjection address =
                    approvalRequestRepository.getDriverAddress(
                            projection.getDriverId());

            //------------------------------------------------------
            // Convert Driver Details to Response DTO
            //------------------------------------------------------

            responseList.add(
                    FmApprovalRequestMapper.toDriverResponse(
                            projection,
                            driverResponse,
                            address));
        }

        //----------------------------------------------------------
        // Processing Completed
        //----------------------------------------------------------

        log.info(
                "Completed Processing DRIVER Approval Requests. " +
                        "Approver Id : {}",
                approverId);
    }
//===============================================================================================
    /**
     * Fetches all rejected approvals.
     *
     * <p>
     * Processing Flow:
     *
     * 1. Fetch all REJECTED Approval Transactions.
     * 2. Fetch corresponding Approval Request.
     * 3. OUTLET details are obtained from FM database.
     * 4. MERCHANT details are obtained from FM database.
     * 5. DRIVER details are obtained from Driver Microservice.
     * 6. Convert each record into common response format.
     *
     * @return List of rejected approval responses
     */
    @Override
    public List<FmRejectedApprovalResponseDTO> getAllRejectedApprovals() {

        log.info("Started fetching all Rejected Approval Requests.");

        //----------------------------------------------------------
        // Fetch Rejected Approval Records
        //----------------------------------------------------------

        List<FmRejectedApprovalProjection> rejectedApprovals =
                approvalRequestRepository.getAllRejectedApprovals();

        //----------------------------------------------------------
        // No Rejected Approvals Found
        //----------------------------------------------------------

        if (rejectedApprovals.isEmpty()) {

            log.info("No Rejected Approval Requests found.");

            return new ArrayList<>();
        }

        log.info("Total Rejected Approval Requests found : {}",
                rejectedApprovals.size());

        //----------------------------------------------------------
        // Prepare Response
        //----------------------------------------------------------

        List<FmRejectedApprovalResponseDTO> responseList = new ArrayList<>();

        //----------------------------------------------------------
        // Process Each Rejected Approval
        //----------------------------------------------------------

        for (FmRejectedApprovalProjection projection : rejectedApprovals) {

            log.debug("Processing Rejected Approval. Transaction Id : {}, " +
                            "Entity Type : {}, Entity Id : {}, Level : {}",
                    projection.getApprovalTransactionsId(),
                    projection.getEntityType(),
                    projection.getEntityId(),
                    projection.getApprovalLevel());

            //------------------------------------------------------
            // Convert Projection → Response DTO
            //------------------------------------------------------

            FmRejectedApprovalResponseDTO response =
                    FmApprovalRequestMapper.toRejectedApprovalResponse(
                            projection);

            //------------------------------------------------------
            // DRIVER
            //
            // Driver belongs to Driver Microservice.
            // Therefore fetch Driver details through Feign.
            //------------------------------------------------------

            if (FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(
                    projection.getEntityType())) {

                log.debug("Fetching Driver Details from Driver Service. Driver Id : {}",
                        projection.getEntityId());

                FmDriverApprovalResponseDTO driver = driverFeignClient.getDriverById(
                                projection.getEntityId());

                //--------------------------------------------------
                // Populate Driver Details
                //
                // Use your actual Driver DTO getter names here.
                //--------------------------------------------------

                if (driver != null) {
                    //----------------------------------------------------------
                    // Prepare Driver Full Name
                    //----------------------------------------------------------

                    String firstName = driver.getFirstName() != null
                                    ? driver.getFirstName() : "";

                    String lastName = driver.getLastName() != null
                                    ? driver.getLastName() : "";

                    String driverName = (firstName + " " + lastName).trim();


                    /*
                     * Replace these getter names if your existing
                     * FmDriverApprovalResponseDTO uses different names.
                     */

                    response.setEntityName(driverName);

                    response.setEmail(driver.getEmail());

                    response.setPhone(driver.getPhoneNumber());

                    response.setProfilePicUrl(driver.getProfilePicUrl());

                    response.setApproved(driver.getIsApproved());
                }

                log.debug("Driver Details fetched successfully. Driver Id : {}",
                        projection.getEntityId());
            }
            //------------------------------------------------------
            // Add Response
            //------------------------------------------------------

            responseList.add(response);
        }

        //----------------------------------------------------------
        // Completed
        //----------------------------------------------------------

        log.info("Completed fetching Rejected Approval Requests. Total Records : {}",
                responseList.size());

        return responseList;
    }
}


