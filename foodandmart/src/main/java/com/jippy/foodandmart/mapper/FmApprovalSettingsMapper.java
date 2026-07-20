package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;
import com.jippy.foodandmart.entity.FmApprovalSettings;

import java.time.LocalDateTime;

/**
 * Mapper class for converting Approval Settings DTOs and Entity.
 */
public class FmApprovalSettingsMapper {

    /**
     * Converts Request DTO to Entity.
     *
     * @param dto Approval Settings request received from UI.
     * @return FmApprovalSettings entity ready to save into database.
     */
    public static FmApprovalSettings mapRequestDtoToEntity(FmApprovalSettingsRequestDTO dto) {

        FmApprovalSettings entity = new FmApprovalSettings();

        entity.setEntityType(dto.getEntityType().toUpperCase());
        entity.setApprovalLevel(dto.getApprovalLevel());
        entity.setApproverRole(dto.getApproverRole().toUpperCase());
        entity.setApproverId(dto.getApproverId());
        entity.setIsActive(dto.getIsActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
//        added new fields
        entity.setWorkflowType(dto.getWorkflowType().toUpperCase());
        entity.setTimeToEscalateInHours(dto.getTimeToEscalateInHours());
        entity.setTriggersActivation(dto.getTriggersActivation());
        entity.setRequiredApprovalsCount(dto.getRequiredApprovalsCount());

        return entity;
    }

    /**
     * Converts Entity to Response DTO.
     *
     * @param entity Saved Approval Settings entity.
     * @return Response DTO sent back to the UI.
     */
    public static FmApprovalSettingsResponseDTO mapEntityToResponseDto(FmApprovalSettings entity) {

        FmApprovalSettingsResponseDTO response = new FmApprovalSettingsResponseDTO();

        response.setApprovalSettingsId(entity.getApprovalSettingsId());
        response.setEntityType(entity.getEntityType());
        response.setApprovalLevel(entity.getApprovalLevel());
        response.setApproverRole(entity.getApproverRole());
        response.setApproverId(entity.getApproverId());
        response.setIsActive(entity.getIsActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedAt(entity.getCreatedAt());
//        added new fields
        response.setWorkflowType(entity.getWorkflowType());
        response.setTimeToEscalateInHours(entity.getTimeToEscalateInHours());
        response.setTriggersActivation(entity.getTriggersActivation());
        response.setRequiredApprovalsCount(entity.getRequiredApprovalsCount());

        return response;
    }
}