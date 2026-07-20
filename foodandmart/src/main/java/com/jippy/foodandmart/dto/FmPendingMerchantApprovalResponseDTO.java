package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Pending Merchant Approval Requests.
 *
 * <p>
 * This DTO contains Merchant details that are returned to the UI
 * for approval by the configured Approver.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmPendingMerchantApprovalResponseDTO {

//     From Merchants Table
    @Schema(description = "Unique identifier of the Merchant.", example = "50")
    private Integer merchantId;

    @Schema(description = "Name of the Merchant.", example = "Friends Restaurant")
    private String merchantName;

    @Schema(description = "Registered email address of the Merchant.", example = "friendsrestaurant@gmail.com")
    private String merchantEmail;

    @Schema(description = "Registered mobile number of the Merchant.", example = "9876543210")
    private String merchantPhone;

    @Schema(description = "Business type of the Merchant.", example = "Restaurant")
    private String merchantBusinessType;

    @Schema(description = "Indicates whether the Merchant is approved.", example = "false")
    private Boolean isApproved;

    @Schema(description = "Date and time when the Merchant was created.",
            example = "2026-07-10T10:30:45")
    private LocalDateTime createdAt;
}
