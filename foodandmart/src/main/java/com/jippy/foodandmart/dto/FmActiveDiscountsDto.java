package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FmActiveDiscountsDto {

    private Integer promotionScheduleId;
    private String sourceType;
    private Integer sourceId;
    private BigDecimal minOrderValue;
    private String priceType;
    private BigDecimal discountAmount;
    private Integer usageLimitPerUser;
    private String couponCode;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String remainingTime;


    //Merchant promotions has these values
    private String planType;
    private  String offerName;

}
