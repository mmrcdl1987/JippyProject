package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalTime;

// 1)projection interface to ---fetch the menu details of an outlet, including outlet information,
// -category information, product information, and their availability based on days and timings.
// 2)projection interface to hold the data coming
// -from JOIN query (Outlet + Category + Product + OutletDays + ProductDays)
// 3)projection -- for Native Query Mapping--JOINS

public interface FmOutletMenuProjection {

//    for outlet table
    Integer getOutletId();
    String getOutletName();
    String getOutletPhone();
    // Outlet is_available
    Boolean getOutletAvailable();


    //    for product_online_pricing table
    Integer productIdFromOnlinePricing();
    BigDecimal getOnlinePrice();

//    for category table
    Integer getCategoryId();
    String getCategoryName();
    // Category is_available
    Boolean getCategoryAvailable();


    //    for product table
    Integer getProductId();
    String getProductName();
    String getDescription();
    BigDecimal getMerchantPrice();
    Boolean getIsVeg();
    Boolean getHasProductVariants();
    // Product is_available
    Boolean getProductAvailable();

//    for outlet_days table
    Boolean getIsOpen();
    LocalTime getOpeningTime();
    LocalTime getClosingTime();
    String getOutletDay();

//    for product_day table
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getProductDay();

}


