package com.jippy.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DivCouponRequestDto {

    private Integer couponId;
    @NotBlank
    private String couponCode;
    @NotNull
    private Integer applicationType;
    @NotNull
    private Integer priceModelId;
    @NotNull
    private Double discountValue;
    private Double minOrderValue;
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private Integer usageLimitPerUser;
    private Integer paymentMode;
    private Integer createdBy;

}