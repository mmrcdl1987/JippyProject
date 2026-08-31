package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Lightweight employee search result used by the employee list UI.
 */
@Data
public class FmEmployeeSearchResponseDTO {

    @Schema(example = "25")
    private Integer employeeId;

    @Schema(example = "Rohan Vadluri")
    private String employeeName;

    @Schema(example = "rohan@gmail.com")
    private String email;

    @Schema(example = "9876543210")
    private String mobileNumber;

    @Schema(example = "Y")
    private String isActive;
}
