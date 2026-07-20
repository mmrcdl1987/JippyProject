package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Response DTO returned after successful Employee creation.
 */
@Data
public class FmCreateEmployeeResponseDTO {

    // --------------------------------------------------
    // Employee Details
    // --------------------------------------------------

    @Schema(example = "25")
    private Integer employeeId;

    @Schema(example = "Rohan Vadluri")
    private String employeeName;

    @Schema(example = "rohan@gmail.com")
    private String email;

    @Schema(example = "9876543210")
    private String mobileNumber;

    // --------------------------------------------------
    // Login Details
    // --------------------------------------------------

    @Schema(example = "rohan_employee")
    private String username;

    /**
     * Password is always masked.
     */
    @Schema(example = "********")
    private String password;

    // --------------------------------------------------
    // Address Details
    // --------------------------------------------------

    @Schema(example = "10-1-20")
    private String buildingNumber;

    @Schema(example = "Main Road")
    private String road;

    @Schema(example = "Near Metro Station")
    private String landmark;

    @Schema(example = "2")
    private Integer stateId;

    @Schema(example = "3")
    private Integer cityId;

    @Schema(example = "13")
    private Integer areaId;

    // --------------------------------------------------
    // Tracking
    // --------------------------------------------------

    @Schema(example = "101")
    private Integer createdBy;

    @Schema(example = "Y")
    private String isActive;

}