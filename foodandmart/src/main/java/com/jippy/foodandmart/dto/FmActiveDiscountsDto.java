package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FmActiveDiscountsDto {

    @Schema(example = "1", description = "Unique identifier for the promotion schedule.")
    private Integer promotionScheduleId;
    @Schema(example = "1", description = "Unique identifier for the promotion.")
    private String sourceType;
    @Schema(example = "12345", description = "Unique identifier for the source associated with this promotion.")
    private Integer sourceId;
    @Schema(example = "100.00", description = "Minimum order value required to avail the promotion.")
    private BigDecimal minOrderValue;
    @Schema(example = "PERCENTAGE", description = "Type of discount applied, e.g., PERCENTAGE or FIXED_AMOUNT.")
    private String priceType;
    @Schema(example = "10.00", description = "Amount of discount applied to the order.")
    private BigDecimal discountAmount;
    @Schema(example = "5", description = "Maximum number of times a user can use this promotion.")
    private Integer usageLimitPerUser;
    @Schema(example = "SAVE10", description = "Coupon code associated with the promotion.")
    private String couponCode;
    @Schema (example = "2024-06-01T00:00:00", description = "Start date and time of the promotion.")
    private LocalDateTime startDateTime;
    @Schema (example = "2024-06-30T23:59:59", description = "End date and time of the promotion.")
    private LocalDateTime endDateTime;
    @Schema(example = "2 days, 5 hours, 30 minutes", description = "Remaining time for the promotion to expire.")
    private String remainingTime;


    //Merchant promotions has these values
    private String planType;
    private  String offerName;

    private Integer maxSelection;
    private String promotionMessage;

}
