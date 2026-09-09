package com.jippy.customerandorder.projection;

import java.time.LocalDateTime;

public interface CoOrderCompleteDetailsProjection {

    // ================= ORDER =================

    String getOrderId();

    LocalDateTime getCreatedAt();

    String getOrderType();

    String getOrderStatus();

    Integer getDriverId();

    // ================= ORDER TIMELINE =================

    LocalDateTime getMerchantAcceptedTime();

    LocalDateTime getFoodPreparationCompletedTime();

    LocalDateTime getDriverOrderAcceptedTime();

    LocalDateTime getDriverOutletReachedTime();

    LocalDateTime getDriverFoodPickupTime();

    LocalDateTime getDriverFoodDeliveredTime();


    // ================= CUSTOMER =================

    Integer getCustomerId();

    String getCustomerName();

    String getEmail();

    String getPhoneNumber();

    String getBuildingName();


    // ================= OUTLET =================

    Integer getOutletId();


    // ================= PAYMENT =================

    Integer getPaymentModeId();

    String getPaymentMode();
}