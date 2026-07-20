package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmApprovalRequestDTO;
import com.jippy.foodandmart.dto.FmDriverApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmLevel1PendingApprovalResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.entity.FmApprovalSettings;
import com.jippy.foodandmart.entity.FmManagerAreas;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.mapper.FmApprovalRequestMapper;
import com.jippy.foodandmart.projections.FmDriverAddressProjection;
import com.jippy.foodandmart.projections.FmDriverLevel1PendingApprovalProjection;
import com.jippy.foodandmart.projections.FmMerchantLevel1PendingApprovalProjection;
import com.jippy.foodandmart.projections.FmOutletLevel1PendingApprovalProjection;
import com.jippy.foodandmart.repository.FmApprovalRequestRepository;
import com.jippy.foodandmart.repository.FmApprovalSettingsRepository;
import com.jippy.foodandmart.repository.FmManagerAreasRepository;
import com.jippy.foodandmart.repository.FmOutletAddressRepository;
import com.jippy.foodandmart.service.IFmApprovalRequestService;
import jakarta.persistence.criteria.From;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


// 4. HELPER METHOD
    private void processApprovalRequest(FmApprovalSettings approvalSettings,
                                        List<FmLevel1PendingApprovalResponseDTO> responseList) {

//        pre-storing the values for Comparision
        String entityType = approvalSettings.getEntityType();
        String approvalLevel = approvalSettings.getApprovalLevel();

        log.info("Processing Entity Type : {}, Approval Level : {}", entityType, approvalLevel);

        if (!FmAppConstants.APPROVAL_LEVEL_1.equalsIgnoreCase(approvalLevel)) {

            log.info("Skipping Unsupported Approval Level : {}", approvalLevel);

            return;
        }

        switch (entityType) {

            case FmAppConstants.TYPE_OUTLET:
                processOutletRequests(approvalLevel, responseList);
                break;

            case FmAppConstants.TYPE_MERCHANT:
                processMerchantRequests(approvalLevel, responseList);
                break;

            case FmAppConstants.TYPE_DRIVER:
                processDriverRequests(approvalLevel, responseList);
                break;

            default:
                log.warn("Unsupported Entity Type : {}", entityType);
        }
    }

// 5. HELPER METHOD
    private void processOutletRequests(String approvalLevel, List<FmLevel1PendingApprovalResponseDTO> responseList) {

        log.info("Fetching Pending Outlet Approval Requests.");

        List<FmOutletLevel1PendingApprovalProjection> outletList =
                approvalRequestRepository.getOutletLevel1PendingRequests(approvalLevel);

        if (outletList.isEmpty()) {

            log.info("No Pending Outlet Approval Requests Found.");

            return;
        }

        log.info("Total Pending Outlet Requests : {}", outletList.size());

        for (FmOutletLevel1PendingApprovalProjection outlet : outletList) {

            responseList.add(FmApprovalRequestMapper.toOutletResponse(outlet));
        }

        log.info("Completed Processing Outlet Approval Requests.");
    }

    // 6. HELPER METHOD
    private void processMerchantRequests(String approvalLevel, List<FmLevel1PendingApprovalResponseDTO> responseList) {

        log.info("Fetching Pending Merchant Approval Requests.");

        List<FmMerchantLevel1PendingApprovalProjection> merchantList =
                approvalRequestRepository.getMerchantLevel1PendingRequests(approvalLevel);

        if (merchantList.isEmpty()) {

            log.info("No Pending Merchant Approval Requests Found.");

            return;
        }

        log.info("Total Pending Merchant Requests : {}", merchantList.size());

        for (FmMerchantLevel1PendingApprovalProjection merchant : merchantList) {

            responseList.add(FmApprovalRequestMapper.toMerchantResponse(merchant));
        }

        log.info("Completed Processing Merchant Approval Requests.");
    }

    // 7. HELPER METHOD
    private void processDriverRequests(
            String approvalLevel, List<FmLevel1PendingApprovalResponseDTO> responseList) {

        // Fetch all pending Driver approval requests
        List<FmDriverLevel1PendingApprovalProjection> driverRequests =
                approvalRequestRepository.getDriverLevel1PendingRequests(approvalLevel);

        for (FmDriverLevel1PendingApprovalProjection projection : driverRequests) {

            // Fetch Driver details from Driver Microservice
            FmDriverApprovalResponseDTO driverResponse =
                    driverFeignClient.getDriverById(projection.getDriverId());

            // Fetch Driver Address from FM database
            FmDriverAddressProjection address =
                    approvalRequestRepository.getDriverAddress(projection.getDriverId());

            responseList.add(FmApprovalRequestMapper.toDriverResponse(
                            projection,
                            driverResponse,
                            address)
            );

        }
    }
}
