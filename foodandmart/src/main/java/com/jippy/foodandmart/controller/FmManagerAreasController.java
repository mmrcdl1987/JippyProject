package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmManagerAreasRequestDTO;
import com.jippy.foodandmart.dto.FmManagerAreasResponseDTO;
import com.jippy.foodandmart.service.IFmManagerAreasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Manager Area Mapping.
 *
 * Provides APIs for assigning
 * one Manager to multiple Areas.
 */
@RestController
@RequestMapping("/api/fm/manager-areas")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Manager Areas Controller",
        description = "APIs for assigning Managers to Areas."
)
public class FmManagerAreasController {

    /**
     * Service for Manager Area Mapping.
     */
    private final IFmManagerAreasService managerAreasService;

    /**
     * Assigns one Manager to multiple Areas.
     *
     * Business Flow:
     * <ul>
     *     <li>Validate Request.</li>
     *     <li>Validate Manager.</li>
     *     <li>Validate Area Ids.</li>
     *     <li>Create Manager-Area mappings.</li>
     *     <li>Return success response.</li>
     * </ul>
     *
     * @param requestDTO contains Manager User Id
     *                   and list of Area Ids.
     * @return Manager Area Mapping Response.
     */
    @Operation(
            summary = "Assign Manager Areas",
            description = "Assigns one Manager to one or more Areas."
    )
    @ApiResponse(responseCode = "200", description = "Manager Areas assigned successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "404", description = "Manager or Area not found.")
    @ApiResponse(responseCode = "409", description = "Manager Area mapping already exists.")
    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    @PostMapping("/assignManagerAreas")
    public ResponseEntity<FmApiResponse<FmManagerAreasResponseDTO>> assignManagerAreas(
            @Valid @RequestBody FmManagerAreasRequestDTO requestDTO) {

        log.info("Received request to assign {} Area(s) to User Id: {}",
                requestDTO.getAreaIds().size(), requestDTO.getUserId());

        FmManagerAreasResponseDTO response =
                managerAreasService.assignManagerAreas(requestDTO);

        log.info("Successfully assigned {} Area(s) to User Id: {} with Approver Name: {}",
                response.getAssignedAreaIds().size(),
                response.getUserId(),
                response.getApproverName());

        return ResponseEntity.ok(FmApiResponse.success(
                        "Manager Areas assigned successfully.", response));
    }

    /**
     * Fetches all Areas assigned to a Manager.
     *
     * @param userId Manager User Id.
     * @return Manager Area mapping details.
     */
    @Operation(
            summary = "Get Assigned Manager Areas",
            description = "Fetches all Area Ids assigned to the given Manager."
    )
    @ApiResponse(responseCode = "200", description = "Manager Areas fetched successfully.")
    @ApiResponse(responseCode = "404", description = "No Area mappings found for the Manager.")
    @GetMapping("/{userId}")
    public ResponseEntity<FmApiResponse<FmManagerAreasResponseDTO>> getAssignedManagerAreas(
            @PathVariable Integer userId) {

        log.info("Received request to fetch assigned Area(s) for User Id: {}", userId);

        FmManagerAreasResponseDTO response =
                managerAreasService.getAssignedManagerAreas(userId);

        log.info("Successfully fetched assigned Area(s) for User Id: {} with Approver Name: {}",
                userId, response.getApproverName());

        return ResponseEntity.ok(FmApiResponse.success(
                "Manager Areas fetched successfully.", response));
    }

    /**
     * Fetches all Areas assigned to a Manager by username.
     *
     * @param username approver username.
     * @return Manager Area mapping details.
     */
    @Operation(
            summary = "Get Assigned Manager Areas by Username",
            description = "Fetches all Area Ids assigned to the given Manager username."
    )
    @ApiResponse(responseCode = "200", description = "Manager Areas fetched successfully.")
    @ApiResponse(responseCode = "404", description = "User or Area mappings not found.")
    @GetMapping("/by-username/{username}")
    public ResponseEntity<FmApiResponse<FmManagerAreasResponseDTO>> getAssignedManagerAreasByUsername(
            @PathVariable String username) {

        log.info("Received request to fetch assigned Area(s) for Username: {}", username);

        FmManagerAreasResponseDTO response =
                managerAreasService.getAssignedManagerAreasByUsername(username);

        log.info("Successfully fetched assigned Area(s) for Username: {} with Approver Name: {}",
                username, response.getApproverName());

        return ResponseEntity.ok(FmApiResponse.success(
                "Manager Areas fetched successfully.", response));
    }

    /**
     * Replaces all Areas assigned to a Manager.
     *
     * @param requestDTO contains Manager User Id and replacement Area Ids.
     * @return updated mapping details.
     */
    @Operation(
            summary = "Update Manager Areas",
            description = "Replaces all existing Area assignments for the given Manager."
    )
    @ApiResponse(responseCode = "200", description = "Manager Areas updated successfully.")
    @ApiResponse(responseCode = "400", description = "Invalid request.")
    @ApiResponse(responseCode = "404", description = "Area not found.")
    @PutMapping("/updateManagerAreas")
    public ResponseEntity<FmApiResponse<FmManagerAreasResponseDTO>> updateManagerAreas(
            @Valid @RequestBody FmManagerAreasRequestDTO requestDTO) {

        log.info("Received request to update assigned Area(s) for User Id: {}",
                requestDTO.getUserId());

        FmManagerAreasResponseDTO response =
                managerAreasService.updateManagerAreas(requestDTO);

        log.info("Successfully updated assigned Area(s) for User Id: {} with Approver Name: {}",
                requestDTO.getUserId(), response.getApproverName());

        return ResponseEntity.ok(FmApiResponse.success(
                "Manager Areas updated successfully.", response));
    }

}
