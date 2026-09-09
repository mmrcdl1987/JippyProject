package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.ApplicationVersionResponseDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateRequestDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateResponseDTO;
import com.jippy.foodandmart.service.AppSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/app-settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application Settings", description = "APIs for application version and update settings")
public class AppSettingsController {

    private final AppSettingsService appSettingsService;

    @GetMapping("/getApplicationVersionByAppType")
    @Operation(summary = "Get application version by app type", description = """
            Fetches application version and update information
            based on the application type.
            
            Supported -- APP TYPES:--
            customer / CUSTOMER
            merchant / MERCHANT
            driver / DRIVER
            
            App type is case-insensitive.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Application version details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid or empty app type"), @ApiResponse(responseCode = "404", description = "Application settings not found")})
    public ResponseEntity<ApplicationVersionResponseDTO> getApplicationVersionByAppType(

            @Parameter(description = "App type. Supported values: customer, merchant, driver. Case-insensitive.", required = true, example = "customer") @RequestParam(name = "appType") String appType) {

        log.info("Received request for application version. appType: {}", appType);

        ApplicationVersionResponseDTO response = appSettingsService.getApplicationVersionByAppType(appType);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //    ======================================================================================
//    ======================================================================================
//    ======================================================================================
    @PutMapping("/updateApplicationVersionByAppType")
    @Operation(summary = "Update application version by app type", description = """
            Updates application version and update settings
            for the specified application type.
            
            Supported app types:
            customer
            merchant
            driver
            
            App type is case-insensitive.
            
            Only the following fields can be updated:
            android_version
            android_build
            ios_version
            ios_build
            min_required_version
            latest_version
            update_url
            force_update
            
            The following fields cannot be updated:
            app_settings_id
            app_name
            package_name
            app_type
            last_updated
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Application version details updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid app type or invalid request data"),
            @ApiResponse(responseCode = "404", description = "Application settings not found")})
    public ResponseEntity<ApplicationVersionUpdateResponseDTO> updateApplicationVersionByAppType(

            @Parameter(description = "App type. Supported values: customer, merchant, driver. Case-insensitive.", required = true, example = "customer") @RequestParam(name = "appType") String appType,

            @Valid @RequestBody ApplicationVersionUpdateRequestDTO requestDTO) {

        log.info("Received request to update application version. appType: {}", appType);

        ApplicationVersionUpdateResponseDTO response =
                appSettingsService.updateApplicationVersionByAppType(appType, requestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
