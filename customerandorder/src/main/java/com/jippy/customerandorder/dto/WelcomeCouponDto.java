package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WelcomeCouponDto {

    private Integer couponId;

    private String couponCode;

    private Integer applicationType;

    private Integer priceModelId;

    private BigDecimal minOrderValue;

    private BigDecimal discountValue;

    private Integer paymentMethod;

    private Integer usageLimitPerUser;

    private Boolean isActive;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String userType;
}