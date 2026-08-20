package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmProductDto {
    @Schema(example = "12345", description = "Unique identifier for the product.")
    private Integer    productId;
    @Schema(example = "123", description = "Unique identifier for the merchant associated with this product.")
    private String     productName;
    @Schema(example = "A delicious and healthy salad made with fresh ingredients.", description = "Description of the product.")
    private String     description;
    // Product image URL from products.image_link
    @Schema(
            example = "https://images.unsplash.com/photo-1544145945-f90425340c7e",
            description = "Product image URL"
    )
    private String imageLink;
    @Schema(example = "10.99", description = "Price of the product for merchants.")
    private BigDecimal merchantPrice;
    @Schema(example = "9.99", description = "Price of the product for online customers.")
    private BigDecimal onlinePrice;
    @Schema(example = "true", description = "Indicates whether this product is vegetarian.")
    private Boolean    isVeg;

//    this field is used to check if product has variants or not.
//    If product has variants then this field will be true otherwise false.
//    if true -> variants list will be populated with product variants details.
    @Schema(example = "true", description = "Indicates whether this product has variants.")
    private Boolean    hasProductVariants;
    // Outlet Category Toggle
    @Schema(example = "true")
    private Boolean isAvailable;

    // Product favourite status for logged-in customer
    @Schema(example = "true",
            description = "Indicates whether this product is marked as favourite by the customer.")
    private Boolean isProductFavourite;

    // All these fields are related to discounts
//    private String sourceType;
//    private Integer sourceId;
//    private BigDecimal minimumOrderValue;
//    private String priceType;
//    private BigDecimal discountValue;
//    private Integer usageLimitPerUser;
//    private String couponCode;
//    private LocalDateTime startDateTime;
//    private LocalDateTime endDateTime;
@Schema(description = "Details of active discounts applicable to this product.")
    private FmActiveDiscountsDto activeDiscountsDto;

//    //Merchant promotions has these values
//    private String planType;
//    private  String offerName;



    @Schema(example = "true", description = "Indicates whether this product is part of a promotion.")
    private List<FmProductVariantDTO> variants;
    @Schema(example = "true", description = "List of product timings indicating when the product is available.")
    private List<FmProductTimingDto> productTimings;
}
