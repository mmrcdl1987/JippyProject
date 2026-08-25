package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateResponseDTO;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.EmailService;
import com.jippy.foodandmart.service.IFmApprovalRequestUpdateService;
import com.jippy.foodandmart.service.IFmUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service Implementation for Approval Request Management.
 * <p>
 * This service is responsible for:
 * <p>
 * 1. Validating Approval Requests.
 * 2. Creating Approval Transaction History.
 * 3. Updating Approval Workflow.
 * 4. Moving Approval Requests to the Next Level.
 * 5. Approving Outlet / Merchant / Driver after the Final Approval Level.
 * 6. Triggering Entity Activation if configured.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmApprovalRequestUpdateServiceImpl
        implements IFmApprovalRequestUpdateService {

    /**
     * Repository for Approval Requests.
     */
    private final FmApprovalRequestRepository approvalRequestRepository;

    /**
     * Repository for Approval Transactions.
     */
    private final FmApprovalTransactionRepository approvalTransactionRepository;

    /**
     * Repository for Approval Settings.
     */
    private final FmApprovalSettingsRepository approvalSettingsRepository;

    /**
     * Repository for Outlet.
     */
    private final FmOutletRepository outletRepository;
    /**
     * Repository for Merchant.
     */
    private final FmMerchantRepository merchantRepository;
    /**
     * Driver Feign Client.
     *
     */
     private final DriverFeignClient driverFeignClient;

    /**
     * User Service used to activate the corresponding
     * User after successful Entity Approval.
     */
    private final IFmUsersService usersService;
    /**
     *
     * Updates Approval Requests.
     */

    private final EmailService emailService;


    @Override
    public FmApprovalRequestUpdateResponseDTO updateApprovalRequestsToApproved(
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Started Processing Approval Requests.");

        //----------------------------------------------------------
        // Step 1 : Validate Status & Rejected Reason
        //----------------------------------------------------------

        validateApprovalStatus(requestDTO);

        //----------------------------------------------------------
        // Step 1.2 : Validate Duplicate Approval Request IDs
        //----------------------------------------------------------

        validateDuplicateApprovalRequestIds(requestDTO);

        //----------------------------------------------------------
        // Step 1.3 : Validate Approver
        //----------------------------------------------------------

        validateApprover(requestDTO.getApproverId());

        //----------------------------------------------------------
        // Step 2 : Validate Approval Request IDs
        //----------------------------------------------------------

        List<FmApprovalRequest> approvalRequests =
                validateApprovalRequestIds(requestDTO);
        //----------------------------------------------------------
        // Prevent Duplicate Approval by Same Approver
        //----------------------------------------------------------

        if (FmAppConstants.APPROVAL_STATUS_APPROVED
                .equalsIgnoreCase(requestDTO.getStatus())) {

            for (FmApprovalRequest approvalRequest : approvalRequests) {

                validateDuplicateApproval(
                        approvalRequest,
                        requestDTO.getApproverId());
            }
        }

        //----------------------------------------------------------
        // Step 3 : Insert Approval Transactions
        //----------------------------------------------------------

        insertApprovalTransactions(approvalRequests, requestDTO);

        //----------------------------------------------------------
        // Step 4 : Process Approval Requests
        //----------------------------------------------------------

        processApprovalRequests(approvalRequests, requestDTO);

        //----------------------------------------------------------
        // Prepare Response
        //----------------------------------------------------------

        FmApprovalRequestUpdateResponseDTO responseDTO = new FmApprovalRequestUpdateResponseDTO();

        responseDTO.setStatus(FmAppConstants.MSG_SUCCESS);
        responseDTO.setMessage(FmAppConstants.MSG_APPROVAL_REQUEST_UPDATED_SUCCESS);

        log.info("Approval Requests Processed Successfully.");

        return responseDTO;
    }
//---------------------------------------------------------------------------------------------------

    // =============================================================
    // Private Helper Methods
    // =============================================================

    /**
     * HELPER METHOD 1
     * /**
     * Validates the Approval Status and Rejected Reason.
     * <p>
     * Business Rules:
     * <p>
     * 1. If Status is APPROVED
     * -> Rejected Reason must be null or empty.
     * <p>
     * 2. If Status is REJECTED
     * -> Rejected Reason is mandatory.
     *
     * @param requestDTO Approval Request Update Request
     */
    private void validateApprovalStatus(FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Validating Approval Status and Rejected Reason.");

        String status = requestDTO.getStatus();
        String rejectedReason = requestDTO.getRejectedReason();

        //----------------------------------------------------------
        // Validation for APPROVED
        //----------------------------------------------------------
        if (FmAppConstants.APPROVAL_STATUS_APPROVED.equalsIgnoreCase(status)) {

            if (rejectedReason != null &&
                    !rejectedReason.trim().isEmpty()) {

                log.error("Rejected Reason should not be provided for APPROVED status.");

                throw new IllegalArgumentException(
                        FmAppConstants.MSG_REJECTED_REASON_NOT_ALLOWED);
            }
        }

        //----------------------------------------------------------
        // Validation for REJECTED
        //----------------------------------------------------------
        if (FmAppConstants.APPROVAL_STATUS_REJECTED.equalsIgnoreCase(status)) {

            if (rejectedReason == null ||
                    rejectedReason.trim().isEmpty()) {


                log.error("Rejected Reason is mandatory for REJECTED status.");

                throw new IllegalArgumentException(
                        FmAppConstants.MSG_REJECTED_REASON_REQUIRED);
            }
        }

        log.info("Approval Status Validation Completed Successfully.");
    }

    /**
     * HELPER METHOD 2
     * Validates all Approval Request IDs.
     * <p>
     * Business Rules:
     * <p>
     * 1. Fetch all Approval Requests using a single database query.
     * 2. Verify every requested Approval Request ID exists.
     * 3. If any ID is invalid, throw an exception.
     * 4. Return the fetched Approval Requests for further processing.
     *
     * @param requestDTO Approval Request Update Request
     * @return List of Approval Requests
     */
    private List<FmApprovalRequest> validateApprovalRequestIds(
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Validating Approval Request IDs.");

        //----------------------------------------------------------
        // Fetch all Approval Requests using one database query
        //----------------------------------------------------------

        List<FmApprovalRequest> approvalRequests =
                approvalRequestRepository.findByApprovalRequestIdIn(
                        requestDTO.getApprovalRequestIds());

        //----------------------------------------------------------
        // Store Missing Approval Request IDs
        //----------------------------------------------------------

        List<Integer> missingRequestIds = new ArrayList<>();

        //----------------------------------------------------------
        // Verify each requested ID exists
        //----------------------------------------------------------

        for (Integer requestId : requestDTO.getApprovalRequestIds()) {

            boolean found = false;

            for (FmApprovalRequest approvalRequest : approvalRequests) {

                if (approvalRequest.getApprovalRequestId().equals(requestId)) {

                    found = true;
                    break;
                }
            }

            if (!found) {
                missingRequestIds.add(requestId);
            }
        }

        //----------------------------------------------------------
        // Throw Exception if any IDs are missing
        //----------------------------------------------------------

        if (!missingRequestIds.isEmpty()) {

            log.error("Invalid Approval Request IDs : {}", missingRequestIds);

            throw new ResourceNotFoundException(
                    FmAppConstants.MSG_APPROVAL_REQUEST_NOT_FOUND + missingRequestIds);
        }

        log.info("All Approval Request IDs are valid.");

        return approvalRequests;
    }

    /**
     * HELPER METHOD 3
     * Inserts Approval Transaction History for every Approval Request.
     * <p>
     * Business Rules:
     * <p>
     * 1. One transaction record will be created for each Approval Request.
     * 2. Entity Type, Entity ID and Current Level are copied
     * from Approval Request.
     * 3. Status comes from Request DTO.
     * 4. Approved By comes from Request DTO.
     * 5. If Status = APPROVED
     * -> Rejected Reason will be NULL.
     * 6. If Status = REJECTED
     * -> Rejected Reason will be stored.
     *
     * @param approvalRequests List of Approval Requests
     * @param requestDTO       Request DTO
     */
    private void insertApprovalTransactions(
            List<FmApprovalRequest> approvalRequests,
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Started inserting Approval Transactions.");

        //----------------------------------------------------------
        // Iterate through every Approval Request
        //----------------------------------------------------------

        for (FmApprovalRequest approvalRequest : approvalRequests) {

            FmApprovalTransaction transaction =
                    FmApprovalTransactionMapper.toEntity(
                            approvalRequest,
                            requestDTO);

            approvalTransactionRepository.save(transaction);

            log.info("Approval Transaction inserted successfully for Entity Type : " +
                            "{}, Entity Id : {}",
                    approvalRequest.getEntityType(), approvalRequest.getEntityId());
        }
    }

    /**
     * HELPER METHOD 4
     * Processes every Approval Request.
     * <p>
     * Business Rules:
     * <p>
     * 1. If Status = REJECTED
     * -> Update Approval Request Status as REJECTED.
     * <p>
     * 2. If Status = APPROVED
     * -> Check Final Approval Level.
     * -> If Current Level is Final Level
     * Update Status = APPROVED.
     * -> Else
     * Move Approval Request to Next Level.
     *
     * @param approvalRequests Approval Request List
     * @param requestDTO       Request DTO
     */
    private void processApprovalRequests(
            List<FmApprovalRequest> approvalRequests,
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Started Processing Approval Requests.");

        for (FmApprovalRequest approvalRequest : approvalRequests) {

            //----------------------------------------------------------
            // If Request is Rejected
            //----------------------------------------------------------

            if (FmAppConstants.APPROVAL_STATUS_REJECTED.equalsIgnoreCase(requestDTO.getStatus())) {

                approvalRequestRepository.updateStatus(
                        approvalRequest.getApprovalRequestId(),
                        FmAppConstants.APPROVAL_STATUS_REJECTED,
                        requestDTO.getApproverId());

                log.info("Approval Request {} Rejected.",
                        approvalRequest.getApprovalRequestId());

                continue;
            }

            //----------------------------------------------------------
            // Request is Approved
            //----------------------------------------------------------

            String currentLevel = approvalRequest.getCurrentLevel();

            //----------------------------------------------------------
            // Fetch Approval Setting for Current Approver
            //----------------------------------------------------------

            FmApprovalSettings approvalSetting = approvalSettingsRepository
                            .findByEntityTypeAndApprovalLevelAndApproverIdAndIsActiveTrue(
                                    approvalRequest.getEntityType(),
                                    currentLevel,
                                    requestDTO.getApproverId())
                            .orElseThrow(() -> {

                log.error("Active Approval Setting Not Found. " +
                                "Entity Type : {}, Approval Level : {}, " +
                                "Approver Id : {}",
                        approvalRequest.getEntityType(),
                        currentLevel,
                        requestDTO.getApproverId());

                return new ResourceNotFoundException(
                        "Active Approval Setting not found for Entity Type : "
                                + approvalRequest.getEntityType()
                                + ", Approval Level : "
                                + currentLevel
                                + ", Approver Id : "
                                + requestDTO.getApproverId());
            });

            //----------------------------------------------------------
            // Check PARALLEL Workflow
            //----------------------------------------------------------

            if (FmAppConstants.WORKFLOW_TYPE_PARALLEL
                    .equalsIgnoreCase(
                            approvalSetting.getWorkflowType())) {

                log.info("PARALLEL Workflow Detected. " +
                                "Approval Request Id : {}, Entity Type : {}, " +
                                "Entity Id : {}, Approval Level : {}, " +
                                "Approver Id : {}, Required Approvals : {}",
                        approvalRequest.getApprovalRequestId(),
                        approvalRequest.getEntityType(),
                        approvalRequest.getEntityId(),
                        currentLevel,
                        requestDTO.getApproverId(),
                        approvalSetting.getRequiredApprovalsCount());

                //----------------------------------------------------------
                // Process PARALLEL Approval
                //----------------------------------------------------------

                processParallelApproval(
                        approvalRequest,
                        approvalSetting.getRequiredApprovalsCount(),
                        requestDTO.getApproverId());

                continue;
            }

            String maximumLevel = approvalSettingsRepository.findMaximumApprovalLevel(
                            approvalRequest.getEntityType());

            Boolean triggerActivation =
                    approvalSettingsRepository.findTriggerActivation(
                            approvalRequest.getEntityType(),
                            currentLevel);

            //----------------------------------------------------------
            // Trigger Activation
            //----------------------------------------------------------

            if (Boolean.TRUE.equals(triggerActivation)) {

                log.info(
                        "Trigger Activation enabled. Approval Request Id : {}, " +
                                "Entity Type : {}, Entity Id : {}, Current Level : {}",
                        approvalRequest.getApprovalRequestId(),
                        approvalRequest.getEntityType(),
                        approvalRequest.getEntityId(),
                        currentLevel);

                //----------------------------------------------------------
                // Approve Actual Business Entity Immediately
                //----------------------------------------------------------

                approveEntity(
                        approvalRequest,
                        requestDTO.getApproverId());

                //----------------------------------------------------------
                // Activate User Immediately
                //----------------------------------------------------------

                usersService.activateUser(
                        approvalRequest.getEntityType(),
                        approvalRequest.getEntityId(),
                        requestDTO.getApproverId());

                //----------------------------------------------------------
                // Check whether Current Level is Maximum Level
                //----------------------------------------------------------

                if (currentLevel.equalsIgnoreCase(maximumLevel)) {

                    //----------------------------------------------------------
                    // Final Level Reached
                    // Complete Approval Request
                    //----------------------------------------------------------

                    approvalRequestRepository.updateStatus(
                            approvalRequest.getApprovalRequestId(),
                            FmAppConstants.APPROVAL_STATUS_APPROVED,
                            requestDTO.getApproverId());

                    log.info("Trigger Activation occurred at Final Level. " +
                                    "Approval Request {} Status Updated to APPROVED.",
                            approvalRequest.getApprovalRequestId());

                } else {

                    //----------------------------------------------------------
                    // Current Level is NOT Final Level
                    // Find Next Approval Level
                    //----------------------------------------------------------

                    String nextLevel =
                            approvalSettingsRepository.findNextApprovalLevel(
                                    approvalRequest.getEntityType(),
                                    currentLevel);

                    //----------------------------------------------------------
                    // Move Approval Request to Next Level
                    // Status remains PENDING
                    //----------------------------------------------------------

                    approvalRequestRepository.updateCurrentLevel(
                            approvalRequest.getApprovalRequestId(),
                            nextLevel,
                            requestDTO.getApproverId());

                    log.info("Trigger Activation completed. " +
                                    "Approval Request {} moved from {} to {}. " +
                                    "Status remains PENDING.",
                            approvalRequest.getApprovalRequestId(),
                            currentLevel,
                            nextLevel);
                }

                //----------------------------------------------------------
                // Trigger Activation Processing Completed
                //----------------------------------------------------------

                log.info("Trigger Activation Processing Completed Successfully. " +
                                "Approval Request Id : {}, Entity Type : {}, Entity Id : {}",
                        approvalRequest.getApprovalRequestId(),
                        approvalRequest.getEntityType(),
                        approvalRequest.getEntityId());

                continue;
            }
            //----------------------------------------------------------
            // Final Approval Level
            //----------------------------------------------------------

            if (currentLevel.equalsIgnoreCase(maximumLevel)) {

                //----------------------------------------------------------
                // Update Approval Request Status as APPROVED
                //----------------------------------------------------------

                approvalRequestRepository.updateStatus(
                        approvalRequest.getApprovalRequestId(),
                        FmAppConstants.APPROVAL_STATUS_APPROVED,
                        requestDTO.getApproverId());

                //----------------------------------------------------------
                // Approve Actual Business Entity
                //----------------------------------------------------------

                approveEntity(approvalRequest, requestDTO.getApproverId());

                //----------------------------------------------------------
                // Activate User after Final Entity Approval
                //----------------------------------------------------------

                usersService.activateUser(approvalRequest.getEntityType(),
                        approvalRequest.getEntityId(),
                        requestDTO.getApproverId());

            //----------------------------------------------------------
            // Final Approval Completed
            //----------------------------------------------------------

                log.info("Approval Request {} completed Final Approval. " +
                                "Entity Type : {}, Entity Id : {}, Approver Id : {}",
                        approvalRequest.getApprovalRequestId(),
                        approvalRequest.getEntityType(),
                        approvalRequest.getEntityId(),
                        requestDTO.getApproverId());

            }

            //----------------------------------------------------------
            // Move to Next Approval Level
            //----------------------------------------------------------

            else {

                String nextLevel =
                        approvalSettingsRepository.findNextApprovalLevel(
                                approvalRequest.getEntityType(),
                                currentLevel);

                approvalRequestRepository.updateCurrentLevel(
                        approvalRequest.getApprovalRequestId(),
                        nextLevel,
                        requestDTO.getApproverId());

                log.info("Approval Request {} moved from {} to {}.",
                        approvalRequest.getApprovalRequestId(),
                        currentLevel, nextLevel);
            }

        }

        log.info("Approval Request Processing Completed.");

    }

    /**
     * HELPER METHOD 5
     * /**
     * Approves the actual business entity.
     * <p>
     * Business Rules:
     * <p>
     * 1. If Entity Type is OUTLET
     * -> Update Outlet as Approved.
     * <p>
     * 2. If Entity Type is MERCHANT
     * -> Update Merchant as Approved.
     * <p>
     * 3. If Entity Type is DRIVER
     * -> Call Driver Service.
     */
    /**
     * Approves the respective entity after successful completion of the approval workflow.
     *
     * @param approvalRequest Approval request details
     * @param approverId      Employee who approved the request
     */
    private void approveEntity(
            FmApprovalRequest approvalRequest,
            Integer approverId) {

        log.info("Started Entity Approval. Entity Type : {}, Entity Id : {}, Approved By : {}",
                approvalRequest.getEntityType(),
                approvalRequest.getEntityId(),
                approverId);

        // ----------------------- OUTLET APPROVAL -----------------------
        if (FmAppConstants.TYPE_OUTLET.equalsIgnoreCase(
                approvalRequest.getEntityType())) {

            FmOutlet outlet = outletRepository.findById(
                    approvalRequest.getEntityId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Outlet not found with id : "
                                    + approvalRequest.getEntityId()
                    )
            );

            outletRepository.approveOutlet(
                    approvalRequest.getEntityId()
            );

            log.info(
                    "Outlet Approved Successfully. Outlet Id : {}",
                    approvalRequest.getEntityId()
            );

            FmMerchant merchant = merchantRepository.findById(
                    outlet.getMerchantId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Merchant not found with id : "
                                    + outlet.getMerchantId()
                    )
            );

            // Email #4 - Outlet is Now Online
            emailService.sendOutletOnlineEmail(
                    outlet.getOutletEmail(),
                    outlet.getOutletName(),
                    merchant.getMerchantName()
            );

            log.info(
                    "OUTLET_ONLINE_EMAIL_SENT | outletId={}, email={}",
                    outlet.getOutletId(),
                    outlet.getOutletEmail()
            );

            return;
        }

        // ----------------------- MERCHANT APPROVAL -----------------------
        if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(
                approvalRequest.getEntityType())) {

            FmMerchant merchant = merchantRepository.findById(
                    approvalRequest.getEntityId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Merchant not found with id : "
                                    + approvalRequest.getEntityId()
                    )
            );

            merchantRepository.approveMerchant(
                    approvalRequest.getEntityId()
            );

            log.info(
                    "Merchant Approved Successfully. Merchant Id : {}",
                    approvalRequest.getEntityId()
            );

            // Email #2 - Merchant Approved
            emailService.sendMerchantApprovedEmail(
                    merchant.getMerchantEmail(),
                    merchant.getMerchantName()
            );

            log.info(
                    "MERCHANT_APPROVED_EMAIL_SENT | merchantId={}, email={}",
                    merchant.getMerchantId(),
                    merchant.getMerchantEmail()
            );

            return;
        }

        // ----------------------- DRIVER APPROVAL -----------------------
        if (FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(approvalRequest.getEntityType())) {

            log.info("Calling Driver Service to Approve Driver. Driver Id : {}",
                    approvalRequest.getEntityId());

            driverFeignClient.approveDriver(
                    approvalRequest.getEntityId());

            log.info("Driver Approved Successfully in Driver Service. Driver Id : {}",
                    approvalRequest.getEntityId());
            return;
        }

        // ----------------------- INVALID ENTITY -----------------------
        log.error("Unsupported Entity Type : {}",
                approvalRequest.getEntityType());

        throw new IllegalArgumentException(
                FmAppConstants.MSG_UNSUPPORTED_ENTITY_TYPE + approvalRequest.getEntityType());
    }

    /**
     * HELPER METHOD 6
     * Validates Duplicate Approval Request IDs.
     * <p>
     * Business Rules:
     * 1. Approval Request IDs should not contain duplicates.
     * 2. If duplicate IDs are found, processing will be stopped.
     * <p>
     * Example:
     * [1,2,3]   -> Valid
     * [1,2,2,3] -> Invalid
     *
     * @param requestDTO Approval Request Update Request
     */
    private void validateDuplicateApprovalRequestIds(
            FmApprovalRequestUpdateRequestDTO requestDTO) {

        log.info("Validating Duplicate Approval Request IDs.");

        List<Integer> approvalRequestIds = requestDTO.getApprovalRequestIds();

        List<Integer> uniqueRequestIds = new ArrayList<>();

        //----------------------------------------------------------
        // Check every Approval Request ID
        //----------------------------------------------------------

        for (Integer approvalRequestId : approvalRequestIds) {

            //------------------------------------------------------
            // Duplicate ID Found
            //------------------------------------------------------

            if (uniqueRequestIds.contains(approvalRequestId)) {

                log.error("Duplicate Approval Request ID Found : {}",
                        approvalRequestId);

                throw new IllegalArgumentException(
                        FmAppConstants.MSG_DUPLICATE_APPROVAL_REQUEST
                                + approvalRequestId);
            }

            uniqueRequestIds.add(approvalRequestId);
        }

        log.info("Duplicate Approval Request ID Validation Completed Successfully.");
    }

    /**
     * HELPER METHOD 7
     * Validates whether the Approver exists in Approval Settings.
     * <p>
     * Business Rules:
     * 1. Approver ID must exist in Active Approval Settings.
     * 2. If Approver is not found, stop processing.
     *
     * @param approverId Approver Employee ID
     */
    private void validateApprover(Integer approverId) {

        log.info("Validating Approver ID : {}", approverId);

        boolean approverExists =
                approvalSettingsRepository.existsByApproverIdAndIsActiveTrue(
                        approverId);

        //----------------------------------------------------------
        // Approver Not Found
        //----------------------------------------------------------

        if (!approverExists) {

            log.error("Approver ID {} does not exist in Approval Settings.",
                    approverId);

            throw new ResourceNotFoundException(
                    FmAppConstants.MSG_APPROVER_NOT_FOUND + approverId);
        }

        log.info("Approver Validation Completed Successfully.");
    }

    /** HELPER METHOD 8
     * Validates that the same Approver has not already
     * approved the same Entity at the same Approval Level.
     *
     * <p>
     * Business Rule:
     * One Approver can contribute only one approval
     * toward the PARALLEL approval threshold.
     *
     * @param approvalRequest Approval Request
     * @param approverId      Current Approver Id
     */
    private void validateDuplicateApproval(
            FmApprovalRequest approvalRequest,
            Integer approverId) {

        log.info(
                "Checking Duplicate Approval. " +
                        "Entity Type : {}, Entity Id : {}, " +
                        "Approval Level : {}, Approver Id : {}",
                approvalRequest.getEntityType(),
                approvalRequest.getEntityId(),
                approvalRequest.getCurrentLevel(),
                approverId);

        //----------------------------------------------------------
        // Check whether this Approver already approved
        //----------------------------------------------------------

        boolean alreadyApproved =
                approvalTransactionRepository
                        .existsByEntityTypeIgnoreCaseAndEntityIdAndApprovalLevelIgnoreCaseAndApprovedByAndStatusIgnoreCase(
                                approvalRequest.getEntityType(),
                                approvalRequest.getEntityId(),
                                approvalRequest.getCurrentLevel(),
                                approverId,
                                FmAppConstants.APPROVAL_STATUS_APPROVED);

        //----------------------------------------------------------
        // Duplicate Approval Found
        //----------------------------------------------------------

        if (alreadyApproved) {

            log.warn("Duplicate Approval Attempt Detected. " +
                            "Entity Type : {}, Entity Id : {}, " +
                            "Approval Level : {}, Approver Id : {}",
                    approvalRequest.getEntityType(),
                    approvalRequest.getEntityId(),
                    approvalRequest.getCurrentLevel(),
                    approverId);

            throw new IllegalArgumentException(
                    "Approver " + approverId
                            + " has already approved Entity Type : "
                            + approvalRequest.getEntityType()
                            + ", Entity Id : "
                            + approvalRequest.getEntityId()
                            + ", Approval Level : "
                            + approvalRequest.getCurrentLevel());
        }

        log.info(
                "Duplicate Approval Check Passed. Approver Id : {}",
                approverId);
    }

    /**  HELPER METHOD 9
     * Processes an Approval Request configured with
     * PARALLEL workflow.
     *
     * <p>
     * PARALLEL Business Rules:
     *
     * 1. Every Approver approval is stored separately
     *    in approval_transactions.
     *
     * 2. Approval Request remains PENDING until the
     *    required approval threshold is reached.
     *
     * 3. current_level NEVER changes in PARALLEL workflow.
     *
     * 4. When required_approvals_count > 0,
     *    that configured number of approvals is required.
     *
     * 5. When required_approvals_count = 0,
     *    ALL active PARALLEL Approvers configured for
     *    the Entity Type and Approval Level must approve.
     *
     * 6. Once the required threshold is reached:
     *    - approval_requests.status = APPROVED
     *    - current_level remains unchanged
     *    - Entity is_approved = true
     *    - User is_active = Y
     *
     * @param approvalRequest        Approval Request
     * @param requiredApprovalsCount Configured Required Approval Count
     * @param approverId             Current Approver Id
     */
    private void processParallelApproval(
            FmApprovalRequest approvalRequest,
            Integer requiredApprovalsCount,
            Integer approverId) {

        //----------------------------------------------------------
        // Extract Approval Request Details
        //----------------------------------------------------------

        String entityType =
                approvalRequest.getEntityType();

        Integer entityId =
                approvalRequest.getEntityId();

        String currentLevel =
                approvalRequest.getCurrentLevel();

        log.info("Started PARALLEL Approval Processing. " +
                        "Approval Request Id : {}, Entity Type : {}, " +
                        "Entity Id : {}, Approval Level : {}, Approver Id : {}",
                approvalRequest.getApprovalRequestId(),
                entityType,
                entityId,
                currentLevel,
                approverId);

        //----------------------------------------------------------
        // Count Completed Approvals
        //
        // The current Approver transaction has already
        // been inserted before this method is called.
        //----------------------------------------------------------

        Integer completedApprovals =
                approvalTransactionRepository
                        .countDistinctApprovedApprovers(
                                entityType,
                                entityId,
                                currentLevel);

        //----------------------------------------------------------
        // Determine Effective Required Approval Count
        //----------------------------------------------------------

        Integer effectiveRequiredApprovals;

        if (requiredApprovalsCount != null
                && requiredApprovalsCount > 0) {

            //------------------------------------------------------
            // Explicit Approval Threshold
            //
            // Example:
            // required_approvals_count = 2
            //------------------------------------------------------

            effectiveRequiredApprovals =
                    requiredApprovalsCount;

            log.info(
                    "Using Configured PARALLEL Approval Threshold : {}",
                    effectiveRequiredApprovals);

        } else {

            //------------------------------------------------------
            // required_approvals_count = 0
            //
            // Business Rule:
            // ALL active PARALLEL Approvers must approve.
            //------------------------------------------------------

            effectiveRequiredApprovals =
                    approvalSettingsRepository
                            .countActiveParallelApprovers(entityType,
                                    currentLevel);

            log.info("Required Approvals Count is 0. " +
                            "All Active PARALLEL Approvers are required. " +
                            "Total Active Approvers : {}",
                    effectiveRequiredApprovals);
        }

        //----------------------------------------------------------
        // Log Current Approval Progress
        //----------------------------------------------------------

        //----------------------------------------------------------
        // Validate Effective Approval Threshold
        //----------------------------------------------------------

        if (effectiveRequiredApprovals == null
                || effectiveRequiredApprovals <= 0) {

            log.error("Invalid PARALLEL Approval Configuration. " +
                            "Entity Type : {}, Approval Level : {}, " +
                            "Required Approvals : {}",
                    entityType,
                    currentLevel,
                    effectiveRequiredApprovals);

            throw new IllegalStateException(
                    "No valid PARALLEL Approvers configured for Entity Type : "
                            + entityType + ", Approval Level : " + currentLevel);
        }
        log.info("PARALLEL Approval Progress. " +
                        "Approval Request Id : {}, " +
                        "Completed Approvals : {}, Required Approvals : {}",
                approvalRequest.getApprovalRequestId(),
                completedApprovals,
                effectiveRequiredApprovals);

        //----------------------------------------------------------
        // Threshold NOT Reached
        //----------------------------------------------------------

        if (completedApprovals < effectiveRequiredApprovals) {

            log.info("PARALLEL Approval Threshold Not Reached. " +
                            "Approval Request Id : {} remains PENDING. " +
                            "Current Level remains unchanged : {}. " +
                            "Completed : {}, Required : {}",
                    approvalRequest.getApprovalRequestId(),
                    currentLevel,
                    completedApprovals,
                    effectiveRequiredApprovals);

            //------------------------------------------------------
            // IMPORTANT:
            //
            // DO NOT update Approval Request.
            // DO NOT change current_level.
            // DO NOT approve Entity.
            // DO NOT activate User.
            //------------------------------------------------------

            return;
        }

        //----------------------------------------------------------
        // Threshold Reached
        //----------------------------------------------------------

        log.info("PARALLEL Approval Threshold Reached. " +
                        "Approval Request Id : {}, Completed : {}, Required : {}",
                approvalRequest.getApprovalRequestId(),
                completedApprovals,
                effectiveRequiredApprovals);

        //----------------------------------------------------------
        // Update ONLY Approval Request Status
        //
        // PENDING -> APPROVED
        //
        // IMPORTANT:
        // current_level must remain unchanged.
        //----------------------------------------------------------

        approvalRequestRepository.updateStatus(
                approvalRequest.getApprovalRequestId(),
                FmAppConstants.APPROVAL_STATUS_APPROVED,
                approverId);

        log.info(
                "Approval Request {} updated to APPROVED. " +
                        "Current Level remains unchanged : {}",
                approvalRequest.getApprovalRequestId(),
                currentLevel);

        //----------------------------------------------------------
        // Approve Actual Business Entity
        //
        // OUTLET   -> is_approved = true
        // MERCHANT -> is_approved = true
        // DRIVER   -> Approve through Driver flow
        //----------------------------------------------------------

        approveEntity(
                approvalRequest,
                approverId);

        log.info(
                "Business Entity Approved Successfully. " +
                        "Entity Type : {}, Entity Id : {}",
                entityType,
                entityId);

        //----------------------------------------------------------
        // Activate User
        //
        // users.is_active : N -> Y
        //----------------------------------------------------------

        usersService.activateUser(
                entityType,
                entityId,
                approverId);

        //----------------------------------------------------------
        // PARALLEL Processing Completed
        //----------------------------------------------------------

        log.info(
                "PARALLEL Approval Completed Successfully. " +
                        "Approval Request Id : {}, Entity Type : {}, " +
                        "Entity Id : {}, Approval Level : {}, " +
                        "Final Status : APPROVED",
                approvalRequest.getApprovalRequestId(),
                entityType,
                entityId,
                currentLevel);
    }

//    /**
//     * HELPER METHOD 10
//     *
//     * Activates the User after successful Entity Approval.
//     *
//     * <p>
//     * Business Rules:
//     *
//     * 1. Supports OUTLET, MERCHANT and DRIVER.
//     * 2. Finds the User using Entity Id and Entity Type.
//     * 3. Changes is_active to Y.
//     * 4. Stores the Approver Id in updated_by.
//     *
//     * @param entityType Entity Type
//     * @param entityId   Entity Id
//     * @param approverId Approver Id
//     */
//    private void activateUser(String entityType, Integer entityId, Integer approverId) {
//
//        log.info("Started User Activation. Entity Type : {}, Entity Id : {}, Approver Id : {}",
//                entityType,
//                entityId,
//                approverId);
//
//        //----------------------------------------------------------
//        // Validate Supported Entity Type
//        //----------------------------------------------------------
//
//        if (!FmAppConstants.TYPE_OUTLET.equalsIgnoreCase(entityType)
//                && !FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(entityType)
//                && !FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(entityType)) {
//
//            log.error("Unsupported Entity Type for User Activation : {}", entityType);
//
//            throw new IllegalArgumentException(
//                    FmAppConstants.MSG_UNSUPPORTED_ENTITY_TYPE + entityType);
//        }
//
//        //----------------------------------------------------------
//        // Activate User
//        //----------------------------------------------------------
//
//        int updatedRows = userRepository.activateUser(entityId, entityType, approverId);
//
//        //----------------------------------------------------------
//        // User Not Found
//        //----------------------------------------------------------
//
//        if (updatedRows == 0) {
//
//            log.warn("No User found for activation. Entity Type : {}, Entity Id : {}",
//                    entityType, entityId);
//
//            return;
//        }
//
//        //----------------------------------------------------------
//        // User Activated Successfully
//        //----------------------------------------------------------
//
//        log.info("User Activated Successfully. Entity Type : {}, Entity Id : {}, Updated By : {}",
//                entityType, entityId, approverId);
//    }

}