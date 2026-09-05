package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.TermsAndConditionsResponseDTO;
import com.jippy.foodandmart.service.TermsAndConditionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/terms-and-conditions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Terms and Conditions",
        description = "APIs for Terms and Conditions and Privacy Policy")
public class TermsAndConditionsController {

    private final TermsAndConditionsService termsAndConditionsService;


    @GetMapping("/getTermsAndConditionsForAppType")
    @Operation(summary = "Get Terms and Conditions or Privacy Policy", description = """
            Fetches Terms and Conditions or Privacy Policy
            based on the application type and policy type.
            
            Supported app types:
            customer
            merchant
            driver
            
            Supported policy types:
            TERMSANDCONDITIONS
            PRIVACYPOLICY
            
            Both parameters are case-insensitive.
            """)
    @ApiResponses({

            @ApiResponse(responseCode = "200", description = "Application policy details fetched successfully"),

            @ApiResponse(responseCode = "400", description = "Invalid app type or policy type"),

            @ApiResponse(responseCode = "404", description = "Application policy details not found")})
    public ResponseEntity<TermsAndConditionsResponseDTO> getTermsAndConditionsForAppType(

            @Parameter(description = "Application type. Supported values: " + "customer, merchant, driver. " + "Case-insensitive.", required = true, example = "merchant") @RequestParam(name = "appType") String appType,

            @Parameter(description = "Policy type. Supported values: " + "TERMSANDCONDITIONS or PRIVACYPOLICY. " + "Case-insensitive.", required = true, example = "TERMSANDCONDITIONS") @RequestParam(name = "appPolicyType") String appPolicyType) {

        log.info("Received request for application policy. " + "appType: {}, appPolicyType: {}", appType, appPolicyType);

        TermsAndConditionsResponseDTO response =
                termsAndConditionsService.getTermsAndConditionsForAppType(appType, appPolicyType);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}