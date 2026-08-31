package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmManagerAreasRequestDTO;
import com.jippy.foodandmart.dto.FmManagerAreasResponseDTO;

/**
 * Service interface for assigning
 * Managers to multiple Areas.
 */
public interface IFmManagerAreasService {

    /**
     * Assigns one Manager to one or more Areas.
     *
     * Business Flow:
     * <ul>
     *     <li>Validate whether the Manager exists. EX:FLEET MANAGER(Employees Table)</li>
     *     <li>Validate whether all Area Ids exist.</li>
     *     <li>Validate duplicate Area Ids in the request.</li>
     *     <li>Validate existing Manager-Area mappings.</li>
     *     <li>Create Manager-Area mappings.</li>
     *     <li>Return the assigned Area details.</li>
     * </ul>
     *
     * @param requestDTO contains Manager User Id
     *                   and list of Area Ids.
     * @return Manager Area assignment response.
     */
    FmManagerAreasResponseDTO assignManagerAreas(FmManagerAreasRequestDTO requestDTO);

    /**
     * Fetches all Areas assigned to a Manager.
     *
     * @param userId Manager User Id.
     * @return Manager Area assignment response.
     */
    FmManagerAreasResponseDTO getAssignedManagerAreas(Integer userId);

    /**
     * Fetches all Areas assigned to a Manager using username.
     *
     * @param username approver username.
     * @return Manager Area assignment response.
     */
    FmManagerAreasResponseDTO getAssignedManagerAreasByUsername(String username);

    /**
     * Replaces all Areas assigned to a Manager.
     *
     * @param requestDTO contains Manager User Id and replacement area ids.
     * @return Manager Area assignment response.
     */
    FmManagerAreasResponseDTO updateManagerAreas(FmManagerAreasRequestDTO requestDTO);

}
