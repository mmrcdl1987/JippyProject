package com.jippy.foodandmart.dto;

import lombok.*;

/**
 * Returned after a single outlet is created.
 * Includes the auto-generated login credentials for the outlet manager.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutletCreatedDTO {

    private Integer outletId;

    private String outletName;

    private Integer merchantId;

    private Integer[] cuisineType;

    private String outletPhone;

    private String isActive;

    // ============================================================
    // VEG / GST
    // ============================================================

    private Boolean isVegOutlet;

    private Boolean isGstApplied;

    // ============================================================
    // OUTLET LOGIN CREDENTIALS
    // ============================================================

    private String outletLoginId;

    private String outletPassword;
}