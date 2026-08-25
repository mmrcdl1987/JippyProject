package com.jippy.foodandmart.projections;

public interface FmProductCategoryProjection {

    Integer getProductId();

    String getProductName();

    Integer getOutletCategoryId();

    Integer getOutletId();

    Integer getCategoryId();

    String getCategoryName();

    String getOutletName();
}