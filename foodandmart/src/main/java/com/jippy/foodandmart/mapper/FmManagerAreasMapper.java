package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmManagerAreasResponseDTO;
import com.jippy.foodandmart.entity.FmManagerAreas;

import java.util.List;

/**
 * Mapper class for Manager Area Mapping.
 * <p>
 * Responsible for converting DTOs
 * and Entity objects.
 */
public class FmManagerAreasMapper {

    /**
     * Converts Request values into
     * Manager Area Entity.
     *
     * @param userId Manager User Id.
     * @param areaId Area Id.
     * @return Manager Area Entity.
     */
    public static FmManagerAreas mapToEntity(Integer userId, Integer areaId) {

        FmManagerAreas entity = new FmManagerAreas();

        entity.setUserId(userId);
        entity.setAreaId(areaId);

        return entity;
    }

    /**
     * Converts assigned Manager and Area Ids
     * into Response DTO.
     *
     * @param userId  Manager User Id.
     * @param areaIds Assigned Area Ids.
     * @return Response DTO.
     */
    public static FmManagerAreasResponseDTO mapToResponseDto(Integer userId, List<Integer> areaIds) {

        FmManagerAreasResponseDTO response = new FmManagerAreasResponseDTO();

        response.setUserId(userId);
        response.setAssignedAreaIds(areaIds);

        return response;
    }

    /**
     * Converts assigned Manager and Area Ids
     * into Response DTO with approver name.
     *
     * @param userId        Manager User Id.
     * @param approverName  Approver name.
     * @param areaIds       Assigned Area Ids.
     * @return Response DTO.
     */
    public static FmManagerAreasResponseDTO mapToResponseDto(Integer userId, String approverName, List<Integer> areaIds) {

        FmManagerAreasResponseDTO response = new FmManagerAreasResponseDTO();
        response.setUserId(userId);
        response.setApproverName(approverName);
        response.setAssignedAreaIds(areaIds);

        return response;
    }

}