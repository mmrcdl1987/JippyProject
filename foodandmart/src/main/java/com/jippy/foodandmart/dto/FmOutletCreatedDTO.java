package com.jippy.foodandmart.dto;

import lombok.*;

/**
 * Returned after a single outlet is created.
 * Includes the auto-generated login credentials for the outlet manager.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletCreatedDTO {

    private Integer outletId;
    private String  outletName;
    private Integer merchantId;
    private String  cuisineType;
    private String  outletPhone;
    private String  isActive;

    /** Auto-generated login ID  e.g. "ravi4567" (name4 + phone-last4) */
    private String outletLoginId;

    /** Auto-generated password e.g. "ravi4567" (same formula, share with manager) */
    private String outletPassword;
}
