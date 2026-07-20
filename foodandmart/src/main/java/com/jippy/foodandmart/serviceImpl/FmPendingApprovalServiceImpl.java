package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmPendingApprovalRequestDTO;
import com.jippy.foodandmart.dto.FmPendingMerchantApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmPendingOutletApprovalResponseDTO;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmPendingApprovalMapper;
import com.jippy.foodandmart.projections.FmPendingMerchantApprovalProjection;
import com.jippy.foodandmart.projections.FmPendingOutletApprovalProjection;
import com.jippy.foodandmart.repository.FmApprovalSettingsRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.service.IFmPendingApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for fetching pending approval requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmPendingApprovalServiceImpl implements IFmPendingApprovalService {

    /**
     * Repository used to fetch Pending Outlet Approval Requests.
     */
    private final FmOutletRepository outletRepository;
    /**
     * Repository used to fetch Pending Merchant Approval Requests.
     */
    private final FmMerchantRepository merchantRepository;
    /**
     * Repository used to validate Approval Settings
     * for the given Approver and Entity Type.
     */
    private  final FmApprovalSettingsRepository approvalSettingsRepository;


    /**
     * Fetches pending approval requests assigned to
     * the given approver for the selected entity type.
     */
    //---------------------------------------------------------------------------------------------
    //------------------------------------FOR OUTLET----------------------------------------------
    //---------------------------------------------------------------------------------------------
    @Override
    public List<FmPendingOutletApprovalResponseDTO> getPendingOutletApprovalRequests
                                                (FmPendingApprovalRequestDTO requestDTO) {

        log.info("Fetching Pending Outlet Approval Requests for Approver Id: {} and Entity Type: {}",
                requestDTO.getApproverId(),
                requestDTO.getEntityType());
        /**
         * Validate whether Approval Settings are configured
         * for the given Approver Id and Entity Type.
         *
         * This validation prevents querying pending approval requests
         * when no approval workflow is configured for the approver.
         */
//        if (!approvalSettingsRepository.existsByApproverIdAndEntityType(
//                requestDTO.getApproverId(), requestDTO.getEntityType())) {
//
//            log.warn("Approval Settings not configured for Approver Id: {} and Entity Type: {}",
//                    requestDTO.getApproverId(), requestDTO.getEntityType());
//
//            throw new ResourceNotFoundException("Approval Settings not configured for Approver Id: "
//              + requestDTO.getApproverId() + " and Entity Type: " + requestDTO.getEntityType());
// }

        validateApprovalSettings(requestDTO);

        /**
         * Fetch all pending approval requests
         * based on Approver Id and Entity Type.
         */
        List<FmPendingOutletApprovalProjection> outletProjections =
                outletRepository.getPendingOutletApprovalRequestsByEntityType
                        (requestDTO.getApproverId(), requestDTO.getEntityType());

        /**
         * Throw exception when no pending approval
         * requests are available.
         */
        validatePendingRecords(outletProjections, requestDTO.getEntityType(),
                requestDTO.getApproverId(), "Outlet");

        /**
         * Response list to be returned to UI.
         */
        List<FmPendingOutletApprovalResponseDTO> responseList = new ArrayList<>();

        /**
         * Convert each Projection into Response DTO.
         */
                for (FmPendingOutletApprovalProjection outletProjection : outletProjections) {

                    FmPendingOutletApprovalResponseDTO response =
                            FmPendingApprovalMapper.mapOutletProjectionToResponseDto(outletProjection);

                    responseList.add(response);
                }

                log.info("Successfully fetched {} Pending Outlet Approval Request(s) " +
                                "for Approver Id: {} and Entity Type: {}",
                     responseList.size(), requestDTO.getApproverId(), requestDTO.getEntityType());

        /**
         * Return the response list to the controller.
         */

        return responseList;
    }

//---------------------------------------------------------------------------------------------
//-------------------------------------FOR MERCHANT----------------------------------------------
//---------------------------------------------------------------------------------------------

    /**
     * Fetches Pending Merchant Approval Requests
     * assigned to the given Approver.
     *
     * Business Flow:
     * 1. Validate Approval Settings.
     * 2. Fetch Pending Merchant Approval Requests.
     * 3. Convert Projection into Response DTO.
     * 4. Return the response to the Controller.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     * @return List of Pending Merchant Approval Requests.
     */
    @Override
    public List<FmPendingMerchantApprovalResponseDTO> getPendingMerchantApprovalRequests(
            FmPendingApprovalRequestDTO requestDTO) {

        log.info("Fetching Pending Merchant Approval Requests for Approver Id: {} and Entity Type: {}",
                requestDTO.getApproverId(),
                requestDTO.getEntityType());

        /**
         * Validate whether Approval Settings are configured
         * for the given Approver Id and Entity Type.
         */
        validateApprovalSettings(requestDTO);

        /**
         * Fetch all Pending Merchant Approval Requests
         * for the specified Approver.
         */
        List<FmPendingMerchantApprovalProjection> merchantProjections =
                merchantRepository.getPendingMerchantApprovalRequestsByEntityType(
                        requestDTO.getApproverId(), requestDTO.getEntityType());

        /**
         * Throw an exception when no Pending Merchant
         * Approval Requests are available.
         */
        validatePendingRecords(merchantProjections, requestDTO.getEntityType(),
                requestDTO.getApproverId(), "Merchant");

        /**
         * Response list returned to the Controller.
         */
        List<FmPendingMerchantApprovalResponseDTO> responseList = new ArrayList<>();

        /**
         * Convert each Projection into Response DTO.
         */
        for (FmPendingMerchantApprovalProjection merchantProjection : merchantProjections) {

            FmPendingMerchantApprovalResponseDTO response =
                    FmPendingApprovalMapper.mapMerchantProjectionToResponseDto(merchantProjection);

            responseList.add(response);
        }

        /**
         * Log the total number of Pending Merchant
         * Approval Requests found.
         */
        log.info("Successfully fetched {} Pending Merchant Approval Request(s).",
                responseList.size());

        return responseList;
    }

// ------------------------------------------------------------------------------------------------
// ----------------------- Common Helper Methods --------------------
// -------------------------------------------------------------------------------------------------
    /**
     * Validates whether Approval Settings are configured
     * for the specified Approver Id and Entity Type.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     */
    private void validateApprovalSettings(
            FmPendingApprovalRequestDTO requestDTO) {

        if (!approvalSettingsRepository.existsByApproverIdAndEntityType(
                requestDTO.getApproverId(), requestDTO.getEntityType())) {

            log.warn("Approval Settings not configured for Approver Id: {} and Entity Type: {}",
                    requestDTO.getApproverId(), requestDTO.getEntityType());

            throw new ResourceNotFoundException("Approval Settings not configured for Approver Id: "
                            + requestDTO.getApproverId()
                            + " and Entity Type: " + requestDTO.getEntityType());
        }

    }
//    -----------------------------------------------------------------------------------------
    /**
     * Validates whether pending approval records exist
     * for the given Entity Type and Approver.
     *
     * @param records list of pending approval records.
     * @param entityType selected entity type.
     * @param approverId approver identifier.
     * @param entityName entity name used in logs and exception messages.
     */
    private void validatePendingRecords(List<?> records, String entityType,
                                        Integer approverId, String entityName) {

        if (records == null || records.isEmpty()) {

            log.warn("No Pending {} Approval Requests found for Approver Id: {} and Entity Type: {}",
                    entityName, approverId, entityType);

            throw new ResourceNotFoundException("No Pending " + entityName
                            + " Approval Requests found for Entity Type: " + entityType
                            + " and Approver Id: " + approverId + " From Last 24 Hours.");
        }
    }

}