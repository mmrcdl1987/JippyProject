package com.jippy.foodandmart.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmFavoriteProductResponseDto {

    // Favourite Details
    private Integer favoriteOutletId;
    private Integer customerId;
    private Integer outletId;
    // productId = favoriteId
    private Integer favoriteId;
    private String favouriteType;
    private Boolean isFavourite;

    // Product Details
//    private Integer productId;
    private String productName;
    private String imageUrl;
    //    from online pricing table
    private BigDecimal onlinePrice;
    private BigDecimal rating;
    private Boolean isVeg;
}