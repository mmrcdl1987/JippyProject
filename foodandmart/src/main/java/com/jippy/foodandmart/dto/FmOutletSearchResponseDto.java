package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmOutletSearchResponseDto {

    private Integer outletId;

    private String outletName;

    private String outletPicUrl;

    private String outletEmail;

    private String alternateOutletPhone;

    private String outletPhone;

    private String outletType;

    private Integer merchantId;

    private Integer[] cuisineType;

    private BigDecimal radius;

    private Double latitude;

    private Double longitude;

    private BigDecimal totalRating;

    private Integer totalReviews;

    private String subscriptionStatus;

    private String promotionStatus;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;

    private String isActive;

    private Integer employeeId;

    private Boolean isApproved;

    private Boolean acceptsScheduledOrders;

    private Boolean isVegOutlet;

    private Boolean isGstApplied;

    private Boolean isToggle;
}