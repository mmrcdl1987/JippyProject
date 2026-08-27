package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmProductUpdateRequestDto {

    @NotBlank(message = "Product Name is required")
    private String productName;
    @Schema(description = "outletCategory must be Integer",example="79")
    @Positive(message = "outletCategoryId must be greater than 0 Negative Driver Id's are Not Allowed")
    private Integer outletCategoryId;

    private String description;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private BigDecimal merchantPrice;

    private String imageLink;

    private String photos;

    private String thumbnail;



    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Merchant Variant Groups with Variant Prices
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}