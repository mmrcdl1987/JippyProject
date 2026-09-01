package com.jippy.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignores any extra fields the order service sends back
public class DivPlaceOrderRequestDto {

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

    private BigDecimal platformFeeTax;

    private Boolean platformFeeToggle;

    private BigDecimal deliveryFee;

    private BigDecimal deliveryFeeTax;

    private BigDecimal surgeFee;

    private BigDecimal surgeFeeTax;

    private Boolean surgeFeeToggle;

    private BigDecimal packagingFee;

    private BigDecimal packagingFeeTax;

    private Boolean packagingFeeToggle;

    private BigDecimal foodTax;

    private BigDecimal totalTax;

    @NotNull(message = "Order total amount is required")
    private BigDecimal orderTotalAmount;

    private BigDecimal couponDiscount;

    /*
     * WALLET USAGE
     */
    private Boolean useWallet;

    private BigDecimal walletAmount;

    /*
     * DELIVERY DISTANCE
     */
    private BigDecimal pickUpDistanceKms;
    private BigDecimal deliveryDistanceKms;
    private BigDecimal pickUpCharges;
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
    private List<DivOrderItemsDto> items;

    /*
     * CUSTOM PLAN
     */
    private List<DivScheduledOrderDto> scheduledOrders;

    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // used only in group orders
    private Integer groupOrderInvitationId;

    private String orderId;

    private String orderStatus;

    private String cookingInstructions;

    private  Boolean isCutleryRequired;

}
