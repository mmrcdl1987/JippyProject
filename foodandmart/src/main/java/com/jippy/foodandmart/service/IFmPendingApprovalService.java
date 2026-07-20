package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmPendingApprovalRequestDTO;
import com.jippy.foodandmart.dto.FmPendingMerchantApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmPendingOutletApprovalResponseDTO;

import java.util.List;

/**
 * Service interface for fetching pending approval requests
 * assigned to an approver based on the selected entity type.
 */
public interface IFmPendingApprovalService {

    /**
     * Fetches pending Outlet approval requests.
     *
     * Business Flow:
     * 1. Validate Approval Settings.
     * 2. Identify the Approver.
     * 3. Determine the Approver's Area.
     * 4. Find all Outlets belonging to that Area.
     * 5. Return only Outlets awaiting approval
     *    that were created within the last 24 hours.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     * @return List of pending Outlet approval requests.
     */
    List<FmPendingOutletApprovalResponseDTO> getPendingOutletApprovalRequests(
            FmPendingApprovalRequestDTO requestDTO);

    /**
     * Fetches pending Merchant approval requests.
     *
     * Business Flow:
     * 1. Validate Approval Settings.
     * 2. Identify the Approver.
     * 3. Determine the Approver's Area.
     * 4. Find all Merchants belonging to that Area.
     * 5. Return only Merchants awaiting approval
     *    that were created within the last 24 hours.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     * @return List of pending Merchant approval requests.
     */
    List<FmPendingMerchantApprovalResponseDTO> getPendingMerchantApprovalRequests(
            FmPendingApprovalRequestDTO requestDTO);

    /**
     * Fetches pending Driver approval requests.
     *
     * Business Flow:
     * 1. Validate Approval Settings.
     * 2. Identify the Approver.
     * 3. Determine the Approver's Area.
     * 4. Find all Drivers belonging to that Area.
     * 5. Return only Drivers awaiting approval
     *    that were created within the last 24 hours.
     *
     * @param requestDTO contains Approver Id and Entity Type.
     * @return List of pending Driver approval requests.
     */
//    List<FmPendingDriverApprovalResponseDTO> getPendingDriverApprovalRequests(
//            FmPendingApprovalRequestDTO requestDTO);

}