package com.jippy.foodandmart.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMerchantSearchResponseDto {

    private Integer merchantId;

    private String merchantName;

    private String merchantEmail;

    private String merchantPhone;

    private String merchantBusinessType;

    private String status;

    private LocalDateTime dateOfBirth;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;

    private String isActive;

    private Boolean isApproved;

    private String profilePicUrl;
}