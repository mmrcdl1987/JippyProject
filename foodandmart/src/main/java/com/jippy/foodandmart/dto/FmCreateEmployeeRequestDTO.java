package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used to create a new Employee.
 * <p>
 * This API creates:
 * 1. Employee
 * 2. Employee Login
 * 3. Employee Address
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmCreateEmployeeRequestDTO {

    // ------------------------------------------------------------------
    // Employee Details
    // ------------------------------------------------------------------

    @Schema(description = "Employee Name", example = "Rohan Vadluri")
    @NotBlank(message = "Employee name is required")
    @Size(max = 100, message = "Employee name must not exceed 100 characters")
    private String employeeName;

    @Schema(description = "Employee Email", example = "rohan@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Employee Mobile Number", example = "9876543210")
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
            message = "Mobile number must be a valid 10-digit Indian mobile number")
    private String mobileNumber;

    // ------------------------------------------------------------------
    // Login Details to users table
    // ------------------------------------------------------------------

    @Schema(description = "Username", example = "rohan_employee")
    @NotBlank(message = "Username is required")
    @Size(max = 100)
    private String username;

    @Schema(description = "Password", example = "Rohan@123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    // ------------------------------------------------------------------
    // Address Details
    // ------------------------------------------------------------------

    @Schema(description = "Building Number", example = "10-1-20")
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @Schema(description = "Road", example = "Main Road")
    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @Schema(description = "Landmark", example = "Near Metro Station")
    @Size(max = 150)
    private String landmark;

    @Schema(description = "State Id", example = "2")
    @NotNull(message = "State ID is required")
    private Integer stateId;

    @Schema(description = "City Id", example = "3")
    @NotNull(message = "City ID is required")
    private Integer cityId;

    @Schema(description = "Area Id", example = "13")
    @NotNull(message = "Area ID is required")
    private Integer areaId;

    // ------------------------------------------------------------------
    // Tracking
    // ------------------------------------------------------------------

    @Schema(description = "Created By", example = "101")
    @NotNull(message = "Created By is required")
    private Integer createdBy;

}