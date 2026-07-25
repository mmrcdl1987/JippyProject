package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.entity.FmApprovalTransaction;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.mapper.FmAutoApprovalTransactionMapper;
import com.jippy.foodandmart.projections.FmPendingApprovalProjection;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmAutoApprovalSchedulerService;
import com.jippy.foodandmart.service.IFmUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for Auto Approval Scheduler.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Fetch pending approval requests eligible for auto approval.</li>
 *     <li>Process each approval request.</li>
 *     <li>Move approval requests to the next level.</li>
 *     <li>Approve entities after the final level.</li>
 * </ul>
 *
 * <p>
 * Phase 1:
 * Only fetch eligible approval requests and log them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmAutoApprovalSchedulerServiceImpl implements IFmAutoApprovalSchedulerService {

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
     * Repository for Outlet Management.
     */
    private final FmOutletRepository outletRepository;

    /**
     * Repository for Merchant Management.
     */
    private final FmMerchantRepository merchantRepository;

    /**
     * Feign Client for Driver Service.
     */
    private final DriverFeignClient driverFeignClient;
    /**
     * User Service used to activate the corresponding
     * User after successful Entity Approval.
     */
    private final IFmUsersService usersService;

//    -------------------------------------------------------------------------------------------------

    /**
     * Fetches all pending approval requests
     * eligible for automatic approval.
     */
    @Override
    public void processAutoApprovalRequests() {

        log.info("==================================================");
        log.info("Auto Approval Scheduler Started");
        log.info("==================================================");

        //----------------------------------------------------------
        // Fetch all eligible pending approval requests
        //----------------------------------------------------------

        List<FmPendingApprovalProjection> pendingApprovalRequests =
                approvalRequestRepository.findPendingRequestsForAutoApproval();

        //----------------------------------------------------------
        // Log total records
        //----------------------------------------------------------

        log.info("Total Pending Approval Requests Found : {}", pendingApprovalRequests.size());

        //----------------------------------------------------------
        // Phase 1
        // Only print records
        //----------------------------------------------------------

        for (FmPendingApprovalProjection approvalRequest : pendingApprovalRequests) {

            log.info("----------------------------------------");

            log.info("Approval Request Id : {}", approvalRequest.getApprovalRequestId());

            log.info("Entity Type : {}", approvalRequest.getEntityType());

            log.info("Entity Id : {}", approvalRequest.getEntityId());

            log.info("Current Level : {}", approvalRequest.getCurrentLevel());

            log.info("Escalation Time (Hours) : {}", approvalRequest.getTimeToEscalateInHours());

            log.info("Trigger Activation : {}", approvalRequest.getTriggersActivation());

            //----------------------------------------------------------
            // Insert Auto Approval Transaction
            //----------------------------------------------------------

            insertAutoApprovalTransaction(approvalRequest);
            //----------------------------------------------------------
            // process Approval Request
            //----------------------------------------------------------

            processApprovalRequest(approvalRequest);
        }


        log.info("==================================================");
        log.info("Auto Approval Scheduler Completed");
        log.info("==================================================");
    }

    /**
     * HELPER METHOD 1
     * <p>
     * Inserts an AUTO_APPROVED transaction
     * for the pending approval request.
     * <p>
     * Business Rules:
     * <p>
     * 1. One transaction will be inserted.
     * 2. Status will be AUTO_APPROVED.
     * 3. Approved By will be System.
     *
     * @param approvalRequest Pending Approval Request
     */
    private void insertAutoApprovalTransaction(FmPendingApprovalProjection approvalRequest) {

        log.info("Started inserting Auto Approval Transaction.");

        //----------------------------------------------------------
        // Convert Projection to Entity
        //----------------------------------------------------------

        FmApprovalTransaction transaction = FmAutoApprovalTransactionMapper.toEntity(approvalRequest);

        //----------------------------------------------------------
        // Save Transaction
        //----------------------------------------------------------

        approvalTransactionRepository.save(transaction);

        //----------------------------------------------------------
        // Log Success
        //----------------------------------------------------------

        log.info("Auto Approval Transaction inserted successfully. " +
                "Approval Request Id : {}, Entity Type : {}, Entity Id : {}",
                approvalRequest.getApprovalRequestId(),
                approvalRequest.getEntityType(),
                approvalRequest.getEntityId());
    }

    /**
     * HELPER METHOD 2
     * <p>
     * Processes the Approval Request after
     * Auto Approval Transaction insertion.
     * <p>
     * Business Rules:
     * <p>
     * 1. Find the Maximum Approval Level.
     * 2. If Current Level is NOT the Maximum Level
     * -> Move to the Next Approval Level.
     * -> Keep Status as PENDING.
     * 3. If Current Level is the Maximum Level
     * -> Do Nothing.
     * -> Final Approval will be handled in Phase 4.
     *
     * @param approvalRequest Pending Approval Request
     */
    private void processApprovalRequest(FmPendingApprovalProjection approvalRequest) {

        log.info("Started Processing Approval Request : {}", approvalRequest.getApprovalRequestId());

        //----------------------------------------------------------
        // Current Approval Level
        //----------------------------------------------------------

        String currentLevel = approvalRequest.getCurrentLevel();

        //----------------------------------------------------------
        // Maximum Approval Level
        //----------------------------------------------------------

        String maximumLevel = approvalSettingsRepository.findMaximumApprovalLevel
                (approvalRequest.getEntityType());

        log.info("Current Level : {}", currentLevel);

        log.info("Maximum Level : {}", maximumLevel);
        //----------------------------------------------------------
        // Fetch Trigger Activation
        //----------------------------------------------------------

        Boolean triggerActivation = approvalSettingsRepository.findTriggerActivation
                (approvalRequest.getEntityType(), currentLevel);

        log.info("Trigger Activation for Entity Type : {}, Level : {} is : {}",
                approvalRequest.getEntityType(), currentLevel, triggerActivation);

        //----------------------------------------------------------
// Trigger Activation Enabled
//----------------------------------------------------------

        if (Boolean.TRUE.equals(triggerActivation)) {

            log.info("Trigger Activation enabled. Approval Request Id : {}, " +
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
                    approvalRequest.getApproverId());

            //----------------------------------------------------------
            // Activate User Immediately
            //----------------------------------------------------------

            usersService.activateUser(
                    approvalRequest.getEntityType(),
                    approvalRequest.getEntityId(),
                    approvalRequest.getApproverId());

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
                        approvalRequest.getApproverId());

                log.info("Trigger Activation occurred at Final Level. " +
                                "Approval Request {} Status Updated to APPROVED.",
                        approvalRequest.getApprovalRequestId());

            } else {

                //----------------------------------------------------------
                // Current Level is NOT Final Level
                // Find Next Approval Level
                //----------------------------------------------------------

                String nextLevel = approvalSettingsRepository.findNextApprovalLevel(
                                approvalRequest.getEntityType(),
                                currentLevel);

                //----------------------------------------------------------
                // Move Approval Request to Next Level
                // Status remains PENDING
                //----------------------------------------------------------

                approvalRequestRepository.updateCurrentLevel(
                        approvalRequest.getApprovalRequestId(),
                        nextLevel,
                        approvalRequest.getApproverId());

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

            return;
        }

        //----------------------------------------------------------
        // Final Approval Level
        //----------------------------------------------------------

        if (currentLevel.equalsIgnoreCase(maximumLevel)) {

            log.info("Approval Request {} reached Final Approval Level. " + "Entity Type : {}, Entity Id : {}, Current Level : {}", approvalRequest.getApprovalRequestId(), approvalRequest.getEntityType(), approvalRequest.getEntityId(), currentLevel);

            //----------------------------------------------------------
            // Update Approval Request Status as APPROVED
            //----------------------------------------------------------

            approvalRequestRepository.updateStatus(approvalRequest.getApprovalRequestId(),
                    FmAppConstants.APPROVAL_STATUS_APPROVED, approvalRequest.getApproverId());

            log.info("Approval Request {} Status Updated to APPROVED.",
                    approvalRequest.getApprovalRequestId());

            //----------------------------------------------------------
            // Approve Actual Business Entity
            //----------------------------------------------------------

            approveEntity(approvalRequest, approvalRequest.getApproverId());

            //----------------------------------------------------------
            // Activate User after Final Entity Approval
            //----------------------------------------------------------

            usersService.activateUser(approvalRequest.getEntityType(),
                    approvalRequest.getEntityId(),
                    approvalRequest.getApproverId());
            //----------------------------------------------------------
            // Final Approval Completed
            //----------------------------------------------------------

            log.info("Final Auto Approval Completed Successfully. " + "Approval Request Id : {}, Entity Type : {}, Entity Id : {}", approvalRequest.getApprovalRequestId(), approvalRequest.getEntityType(), approvalRequest.getEntityId());

            return;
        }
        //----------------------------------------------------------
        // Find Next Approval Level
        //----------------------------------------------------------

        String nextLevel = approvalSettingsRepository.
                findNextApprovalLevel(approvalRequest.getEntityType(),
                        currentLevel);

        log.info("Next Approval Level : {}", nextLevel);

        //----------------------------------------------------------
        // Update Approval Request
        //----------------------------------------------------------

        approvalRequestRepository.updateCurrentLevel(approvalRequest.getApprovalRequestId(),
                nextLevel,
                approvalRequest.getApproverId());

        log.info("Approval Request {} moved from {} to {}",
                approvalRequest.getApprovalRequestId(),
                currentLevel, nextLevel);

    }

//    /**
//     * HELPER METHOD 3
//     * <p>
//     * Activates the User based on Entity Type and Entity Id.
//     * <p>
//     * Business Rules:
//     * <p>
//     * 1. Entity Type must be OUTLET, MERCHANT or DRIVER.
//     * 2. User must exist for the given Entity Id and Entity Type.
//     * 3. User Status will be updated from N to Y.
//     * 4. Updated By will be the Approver configured
//     * in Approval Settings for the current Approval Level.
//     *
//     * @param entityType Type of Entity
//     * @param entityId   Entity Id
//     * @param approverId Approver Id configured in Approval Settings
//     */
//    private void activateUser(String entityType, Integer entityId, Integer approverId) {
//
//        log.info("Started User Activation. Entity Type : {}, Entity Id : {}, Approver Id : {}",
//                entityType, entityId, approverId);
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
//            throw new IllegalArgumentException(FmAppConstants.MSG_UNSUPPORTED_ENTITY_TYPE + entityType);
//        }
//
//        //----------------------------------------------------------
//        // Activate User
//        //----------------------------------------------------------
//
//        int updatedRows = userRepository.activateUser(entityId, entityType, approverId);
//
//        //----------------------------------------------------------
//        // Validate Update
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

    /**
     * HELPER METHOD 4
     * <p>
     * Approves the actual Business Entity after
     * successful completion of the Auto Approval Workflow.
     * <p>
     * Business Rules:
     * <p>
     * 1. If Entity Type is OUTLET
     * -> Update Outlet is_approved = true.
     * <p>
     * 2. If Entity Type is MERCHANT
     * -> Update Merchant is_approved = true.
     * <p>
     * 3. If Entity Type is DRIVER
     * -> Call Driver Microservice
     * -> Update Driver is_approved = true.
     *
     * @param approvalRequest Pending Approval Request
     * @param approverId      System Approver Id
     */
    private void approveEntity(FmPendingApprovalProjection approvalRequest, Integer approverId) {

        log.info("Started Entity Approval. Entity Type : {}, Entity Id : {}, Approved By : {}",
                approvalRequest.getEntityType(), approvalRequest.getEntityId(), approverId);

        //----------------------------------------------------------
        // OUTLET APPROVAL
        //----------------------------------------------------------

        if (FmAppConstants.TYPE_OUTLET.equalsIgnoreCase(approvalRequest.getEntityType())) {

            outletRepository.approveOutlet(approvalRequest.getEntityId());

            log.info("Outlet Approved Successfully. Outlet Id : {}", approvalRequest.getEntityId());

            return;
        }

        //----------------------------------------------------------
        // MERCHANT APPROVAL
        //----------------------------------------------------------

        if (FmAppConstants.TYPE_MERCHANT.equalsIgnoreCase(approvalRequest.getEntityType())) {

            merchantRepository.approveMerchant(approvalRequest.getEntityId());

            log.info("Merchant Approved Successfully. Merchant Id : {}", approvalRequest.getEntityId());

            return;
        }

        //----------------------------------------------------------
        // DRIVER APPROVAL
        //----------------------------------------------------------

        if (FmAppConstants.TYPE_DRIVER.equalsIgnoreCase(approvalRequest.getEntityType())) {

            log.info("Calling Driver Service to Approve Driver. Driver Id : {}",
                    approvalRequest.getEntityId());

            driverFeignClient.approveDriver(approvalRequest.getEntityId());

            log.info("Driver Approved Successfully in Driver Service. Driver Id : {}",
                    approvalRequest.getEntityId());

            return;
        }

        //----------------------------------------------------------
        // UNSUPPORTED ENTITY TYPE
        //----------------------------------------------------------

        log.error("Unsupported Entity Type : {}", approvalRequest.getEntityType());

        throw new IllegalArgumentException(FmAppConstants.MSG_UNSUPPORTED_ENTITY_TYPE +
                approvalRequest.getEntityType());
    }
}