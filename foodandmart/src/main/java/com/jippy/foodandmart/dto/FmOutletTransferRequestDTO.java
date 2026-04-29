package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletTransferRequestDTO {

    @NotNull(message = "Outlet ID is required")
    private Integer outletId;

    @NotNull(message = "Target merchant ID is required")
    private Integer toMerchantId;

    /** Optional free-text reason for the transfer (audit trail). */
    private String transferReason;

    /** ID of the admin / user performing the transfer (for audit). */
    private Integer transferredBy;
}
