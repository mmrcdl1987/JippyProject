package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private Integer maxSelection;

    /**
     * Comma-separated meal type slot IDs from the database.
     * Example: "1,5,8"
     */
    private String mealTypeSlotIdsStr;

    /**
     * Promotional message configured for the campaign.
     */
    private String promotionMessage;
}