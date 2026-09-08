package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivePromotionDto {

    private Integer productId;

    private String promotionSourceType;

    private Integer sourceId;

    private String promotionName;

    private String couponCode;

    private BigDecimal discountValue;

    private String discountType;

    private BigDecimal minimumOrderValue;

    private Integer maxSelection;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;
}