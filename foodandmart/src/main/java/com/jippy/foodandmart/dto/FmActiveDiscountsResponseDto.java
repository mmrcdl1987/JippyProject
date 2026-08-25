package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FmActiveDiscountsResponseDto {

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
    private String mealTypeSlotIdsStr;
    private Integer maxSelection;
    private String promotionMessage;


    // Helper getter to parse "1,5" into List<Integer> [1, 5]
    public List<Integer> getMealTypeSlotIds() {
        if (mealTypeSlotIdsStr == null || mealTypeSlotIdsStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(mealTypeSlotIdsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

}
