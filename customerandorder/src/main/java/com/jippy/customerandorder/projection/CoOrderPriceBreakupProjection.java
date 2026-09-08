package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOrderPriceBreakupProjection {

    String getOrderId();

    BigDecimal getOrderAmount();

    BigDecimal getPickUpDistanceInKms();

    BigDecimal getDeliveryDistanceInKms();

    BigDecimal getPickUpCharges();

    BigDecimal getDriverDeliveryFee();

    BigDecimal getCustomerDeliveryFee();

    BigDecimal getTotalDeliveryFee();

    BigDecimal getPlatformFee();

    BigDecimal getPlatformFeeTax();

    BigDecimal getSurgeFee();

    BigDecimal getSurgeFeeTax();

    BigDecimal getPackagingFee();

    BigDecimal getPackagingFeeTax();

    BigDecimal getFoodTax();

    BigDecimal getTotalTax();

    BigDecimal getTip();

    BigDecimal getCouponDiscount();

    BigDecimal getWalletAmount();

    BigDecimal getOrderAmountDiscounted();

    BigDecimal getOrderTotalAmount();

    BigDecimal getCustomerDeliveryFeeTax();
}