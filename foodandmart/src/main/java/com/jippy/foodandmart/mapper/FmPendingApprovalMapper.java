package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmPendingMerchantApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmPendingOutletApprovalResponseDTO;
import com.jippy.foodandmart.projections.FmPendingMerchantApprovalProjection;
import com.jippy.foodandmart.projections.FmPendingOutletApprovalProjection;

/**
 * Mapper for Pending Approval Projection.
 */
public class FmPendingApprovalMapper {

    /**
     * Converts Projection into Response DTO.
     *
     * @param projection database projection
     * @return response dto
     */
    public static FmPendingOutletApprovalResponseDTO mapOutletProjectionToResponseDto(
            FmPendingOutletApprovalProjection projection){

        FmPendingOutletApprovalResponseDTO response = new FmPendingOutletApprovalResponseDTO();

        response.setOutletId(projection.getOutletId());

        response.setOutletName(projection.getOutletName());

        response.setMerchantId(projection.getMerchantId());

        response.setCuisineType(projection.getCuisineType());

        response.setOutletPhone(projection.getOutletPhone());

        response.setOutletEmail(projection.getOutletEmail());

        response.setIsApproved(projection.getIsApproved());

        response.setCreatedAt(projection.getCreatedAt());

        return response;
    }
//    ---------------------------------------------------------------------------------------------------
    /**
     * Converts Merchant Pending Approval Projection into Response DTO.
     *
     * @param projection Merchant Pending Approval Projection returned from database.
     * @return Merchant Pending Approval Response DTO sent to the UI.
     */
    public static FmPendingMerchantApprovalResponseDTO mapMerchantProjectionToResponseDto(
            FmPendingMerchantApprovalProjection projection) {

        FmPendingMerchantApprovalResponseDTO response = new FmPendingMerchantApprovalResponseDTO();

        response.setMerchantId(projection.getMerchantId());

        response.setMerchantName(projection.getMerchantName());

        response.setMerchantEmail(projection.getMerchantEmail());

        response.setMerchantPhone(projection.getMerchantPhone());

        response.setMerchantBusinessType(projection.getMerchantBusinessType());

        response.setIsApproved(projection.getIsApproved());

        response.setCreatedAt(projection.getCreatedAt());

        return response;
    }

}