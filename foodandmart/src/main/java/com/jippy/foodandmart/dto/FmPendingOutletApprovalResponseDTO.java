package com.jippy.foodandmart.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO representing one pending approval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmPendingOutletApprovalResponseDTO {

    private Integer outletId;

    private String outletName;

    private Integer merchantId;

    private String cuisineType;

    private String outletPhone;

    private String outletEmail;
    /**
     * Approval Status.
     */
    private Boolean isApproved;
    /**
     * Outlet Registration Time.
     */
    private LocalDateTime createdAt;

}