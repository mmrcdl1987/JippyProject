package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmPublicNearbyOutletDto {

    private Integer outletId;
    private String outletName;
    private Integer merchantId;
    private String outletPhone;
    private BigDecimal radius;
    private String subscriptionStatus;
    private String promotionStatus;
    private Double review;
    private Boolean isActive;
    private Boolean isApproved;
    private Double distanceKm;
}
