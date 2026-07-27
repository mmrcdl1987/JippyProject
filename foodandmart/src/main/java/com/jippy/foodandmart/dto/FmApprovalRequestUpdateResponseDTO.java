package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Response DTO returned after updating approval requests.
 */
@Data
@Schema(description = "Approval Request Update Response")
public class FmApprovalRequestUpdateResponseDTO {

    @Schema(description = "Response Status", example = "SUCCESS")
    private String status;

    @Schema(description = "Response Message", example = "Approval Requests Updated Successfully.")
    private String message;
}