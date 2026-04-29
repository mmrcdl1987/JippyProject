package com.jippy.foodandmart.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletTransferResponseDTO {

    private Integer transferId;
    private Integer outletId;
    private String  outletName;

    private Integer fromMerchantId;
    private String  fromMerchantName;

    private Integer toMerchantId;
    private String  toMerchantName;

    private String  transferReason;
    private String  transferStatus;
    private LocalDateTime transferredAt;
}
