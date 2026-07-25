package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApprovalRequestUpdateRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalRequestUpdateResponseDTO;

/**
 * Service Interface for Approval Request Update.
 */
public interface IFmApprovalRequestUpdateService {

    /**
     * Updates Approval Requests by Approving or Rejecting them.
     *
     * @param requestDTO Approval Request Details
     * @return Response DTO
     */
    FmApprovalRequestUpdateResponseDTO updateApprovalRequestsToApproved(
            FmApprovalRequestUpdateRequestDTO requestDTO);

}