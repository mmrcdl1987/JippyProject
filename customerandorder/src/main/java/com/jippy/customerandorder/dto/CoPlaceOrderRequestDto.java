package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoPlaceOrderRequestDto {

    @NotNull(message = "Outlet id is required")
    private Integer outletId;

    @NotNull(message = "Customer id is required")
    private Integer customerId;

    @NotNull(message = "Customer delivery address id is required")
    private Integer customerDeliveryAddressId;

    @NotNull(message = "Customer phone is required")
    private String customerPhone;

    @NotNull(message = "Payment mode id is required")
    private Integer paymentModeId;

    private Integer couponId;

    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;

    private BigDecimal platformFee;

    private BigDecimal deliveryFee;

    private BigDecimal surgeFee;

    private BigDecimal packagingFee;

    private BigDecimal gst;

    @NotNull(message = "Order total amount is required")
    private BigDecimal orderTotalAmount;

    private BigDecimal couponDiscount;

    /*
     * DELIVERY DISTANCE
     */
    private BigDecimal distanceKms;

    /*
     * NORMAL
     * SCHEDULED_RECURRING
     * SCHEDULED_CUSTOM_PLAN
     */
    @NotNull(message = "Order type is required")
    private String orderType;

    /*
     * RECURRING ONLY
     */
    private LocalDateTime scheduledDeliveryDateTime;

    /*
     * RECURRING ONLY
     */
    private LocalDateTime subscriptionStartDate;

    /*
     * RECURRING ONLY
     */
    private LocalDateTime subscriptionEndDate;

    /*
     * BREAKFAST
     * LUNCH
     * DINNER
     */
    private String mealPreference;

    /*
     * RECURRING
     */
    private List<CoOrderItemDto> items;

    /*
     * CUSTOM PLAN
     */
    private List<CoScheduledOrderDto> scheduledOrders;

    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // used only in group orders
    private Integer groupOrderInvitationId;
}