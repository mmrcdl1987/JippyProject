package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoOrderCompleteDetailsResponseDto {

    // ================= ORDER =================

    private String orderId;

    /*
     * Replaced createdBy with createdAt.
     */
    private LocalDateTime createdAt;

    private String orderType;

    private String orderStatus;

    private String paymentMode;

    // ================= ORDER TIMELINE =================

    private LocalDateTime merchantAcceptedTime;

    private LocalDateTime foodPreparationCompletedTime;

    private LocalDateTime driverOrderAcceptedTime;

    private LocalDateTime driverOutletReachedTime;

    private LocalDateTime driverFoodPickupTime;

    private LocalDateTime driverFoodDeliveredTime;



    // ================= CUSTOMER =================

    private CoCustomerDetailsDto customer;


    // ================= OUTLET =================

    private CoOutletDetailsDto outlet;


    // ================= DRIVER =================

    private CoDriverDetailsDto driver;


    // ================= ITEMS =================

    private List<CoOrderItemDetailsDto> items;


    // ================= PRICE BREAKUP =================

    private CoOrderPriceBreakupDto priceBreakup;


    // ================= REFUND =================

    private CoRefundDetailsDto refund;
}