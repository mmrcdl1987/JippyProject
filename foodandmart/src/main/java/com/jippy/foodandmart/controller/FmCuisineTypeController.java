package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmCuisineTypeRequestDTO;
import com.jippy.foodandmart.dto.FmCuisineTypeResponseDTO;
import com.jippy.foodandmart.service.FmCuisineTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fm/cuisine-types")
@RequiredArgsConstructor
public class FmCuisineTypeController {

    private final FmCuisineTypeService cuisineTypeService;

    // CREATE
    @Operation(summary = "Create cuisine type", description = "Creates a new cuisine type.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Cuisine type created successfully"), @ApiResponse(responseCode = "400", description = "Validation failed"), @ApiResponse(responseCode = "409", description = "Cuisine type already exists")})
    @PostMapping
    public ResponseEntity<FmApiResponse<FmCuisineTypeResponseDTO>> createCuisineType(

            @Valid @RequestBody FmCuisineTypeRequestDTO dto) {

        log.info("Received request to create cuisine type. name={}", dto.getCuisineTypesName());

        FmCuisineTypeResponseDTO response = cuisineTypeService.createCuisineType(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(FmApiResponse.success("Cuisine type created successfully", response));
    }

    // GET ALL

    @Operation(summary = "Get all cuisine types", description = "Returns all cuisine types.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cuisine types fetched successfully")})
    @GetMapping
    public ResponseEntity<FmApiResponse<List<FmCuisineTypeResponseDTO>>> getAllCuisineTypes() {

        log.info("Received request to fetch all cuisine types.");

        List<FmCuisineTypeResponseDTO> response = cuisineTypeService.getAllCuisineTypes();

        return ResponseEntity.ok(FmApiResponse.success("Cuisine types fetched successfully", response));
    }


    // GET BY ID

    @Operation(summary = "Get cuisine type by ID", description = "Returns a cuisine type by its ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cuisine type fetched successfully"), @ApiResponse(responseCode = "404", description = "Cuisine type not found")})
    @GetMapping("/{cuisineTypesId}")
    public ResponseEntity<FmApiResponse<FmCuisineTypeResponseDTO>> getCuisineTypeById(

            @PathVariable Integer cuisineTypesId) {

        log.info("Received request to fetch cuisine type. id={}", cuisineTypesId);

        FmCuisineTypeResponseDTO response = cuisineTypeService.getCuisineTypeById(cuisineTypesId);

        return ResponseEntity.ok(FmApiResponse.success("Cuisine type fetched successfully", response));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Operation(summary = "Update cuisine type", description = "Updates an existing cuisine type.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cuisine type updated successfully"), @ApiResponse(responseCode = "400", description = "Validation failed"), @ApiResponse(responseCode = "404", description = "Cuisine type not found"), @ApiResponse(responseCode = "409", description = "Cuisine type already exists")})
    @PutMapping("/{cuisineTypesId}")
    public ResponseEntity<FmApiResponse<FmCuisineTypeResponseDTO>> updateCuisineType(

            @PathVariable Integer cuisineTypesId,

            @Valid @RequestBody FmCuisineTypeRequestDTO dto) {

        log.info("Received request to update cuisine type. id={}", cuisineTypesId);

        FmCuisineTypeResponseDTO response = cuisineTypeService.updateCuisineType(cuisineTypesId, dto);

        return ResponseEntity.ok(FmApiResponse.success("Cuisine type updated successfully", response));
    }
}