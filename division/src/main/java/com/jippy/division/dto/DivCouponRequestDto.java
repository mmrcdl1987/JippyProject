package com.jippy.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DivCouponRequestDto {

    private Integer couponId;

    @NotBlank(message = "Coupon code is required")
    private String couponCode;

    @NotNull(message = "Application type is required")
    private Integer applicationType;

    @NotNull(message = "Price model is required")
    private Integer priceModelId;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00")
    private BigDecimal minOrderValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private Integer usageLimitPerUser;

    private Integer paymentMethod;

    private String userType;

    private Integer createdBy;

    private Integer updatedBy;
}