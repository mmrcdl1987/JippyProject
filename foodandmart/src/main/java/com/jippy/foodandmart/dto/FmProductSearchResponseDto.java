package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO used for product name search API.
 *
 * Contains product information required by the
 * search API without exposing JPA relationships.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmProductSearchResponseDto {

    private Integer productId;

    private Integer outletCategoryId;

    private String productName;

    private String description;

    private BigDecimal merchantPrice;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private String imageLink;

    private Boolean isToggle;

    private String isActive;

    private BigDecimal rating;

    private Boolean isImageDescUpdated;

    private String productType;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}