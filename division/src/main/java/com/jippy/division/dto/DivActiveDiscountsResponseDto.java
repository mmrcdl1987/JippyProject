package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DivActiveDiscountsResponseDto {

    private Integer promotionScheduleId;
    private Integer outletId;
    private Integer productId;
    private String sourceType;
    private Integer sourceId;
    private BigDecimal minOrderValue;
    private String priceType;
    private BigDecimal discountAmount;
    private Integer usageLimitPerUser;
    private String couponCode;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    // String coming directly from database query (e.g., "1,5")
    private String mealTypeSlotIdsStr;


}
