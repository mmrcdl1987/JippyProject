package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoPlaceOrderResponseDto {

    /*
     * SUCCESS MESSAGE
     */
    private String message;

    /*
     * ONLY FOR
     * SCHEDULED_RECURRING
     * SCHEDULED_CUSTOM_PLAN
     */
    private Integer mealSubscriptionId;

    /*
     * ALL CREATED ORDER IDS
     */
    private List<String> orderIds;

    /*
     * NORMAL
     * SCHEDULED_RECURRING
     * SCHEDULED_CUSTOM_PLAN
     */
    private String orderType;

    /*
     * TOTAL ORDERS CREATED
     */
    private Integer totalOrdersCreated;

    /*
     * RESPONSE CREATED TIME
     */
    private LocalDateTime createdAt;

    private String orderId;

    private BigDecimal orderTotalAmount;

    private BigDecimal walletDiscount;

    private String orderStatus;

    private Integer paymentModeId;

    private Integer outletId;

    private Integer customerId;

}
