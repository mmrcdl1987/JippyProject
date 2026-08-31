package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FmPublicActiveDiscountsDto {

    @Schema(example = "1")
    private Integer promotionScheduleId;

    @Schema(example = "1")
    private String sourceType;

    @Schema(example = "12345")
    private Integer sourceId;

    @Schema(example = "100")
    private BigDecimal minOrderValue;

    @Schema(example = "PERCENTAGE")
    private String priceType;

    @Schema(example = "10")
    private BigDecimal discountAmount;

    @Schema(example = "5")
    private Integer usageLimitPerUser;

    @Schema(example = "SAVE10")
    private String couponCode;

    @Schema(example = "2024-06-01T00:00:00")
    private LocalDateTime startDateTime;

    @Schema(example = "2024-06-30T23:59:59")
    private LocalDateTime endDateTime;

    @Schema(example = "2")
    private String remainingTime;

    @Schema(example = "string")
    private String planType;

    @Schema(example = "string")
    private String offerName;

    @Schema(example = "1073741824")
    private Integer maxSelection;

    @Schema(example = "string")
    private String promotionMessage;
}
