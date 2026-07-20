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

        log.info("Successfully assigned {} Area(s) to User Id: {}",
                response.getAssignedAreaIds().size(),
                response.getUserId());

        return ResponseEntity.ok(FmApiResponse.success(
                        "Manager Areas assigned successfully.", response));
    }

}