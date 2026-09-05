package com.jippy.customerandorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoPlaceOrderRequestDto {

    // ================= ORDER =================

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

    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;

    private BigDecimal orderAmountDiscounted;

    private Integer couponId;

    private BigDecimal couponDiscount;

    @NotNull(message = "Order total amount is required")
    private BigDecimal orderTotalAmount;

    // ================= DRIVER DELIVERY =================

    private BigDecimal pickUpDistanceKms;

    private BigDecimal deliveryDistanceKms;

    private BigDecimal pickUpCharges;

    /**
     * Driver delivery charge.
     * No GST is applied to this amount.
     */
    private BigDecimal driverDeliveryFee;

    /**
     * Total driver delivery fee.
     * No driver delivery tax.
     */
    private BigDecimal totalDeliveryFee;

    // ================= CUSTOMER DELIVERY =================

    /**
     * Customer delivery charge after free-distance benefit.
     */
    private BigDecimal customerDeliveryFee;

    /**
     * GST on customer delivery.
     */
    private BigDecimal customerDeliveryFeeTax;

    // ================= PLATFORM FEE =================

    private BigDecimal platformFee;

    private BigDecimal platformFeeTax;

    private Boolean platformFeeToggle;

    // ================= SURGE FEE =================

    private BigDecimal surgeFee;

    private BigDecimal surgeFeeTax;

    private Boolean surgeFeeToggle;

    // ================= PACKAGING FEE =================

    private BigDecimal packagingFee;

    private BigDecimal packagingFeeTax;

    private Boolean packagingFeeToggle;

    // ================= FOOD TAX =================

    private BigDecimal foodTax;

    private BigDecimal totalTax;

    // ================= WALLET =================

    private Boolean useWallet;

    private BigDecimal walletAmount;

    // ================= TIP =================

    private BigDecimal tip;

    // ================= ORDER TYPE =================

    /**
     * NORMAL
     * SCHEDULED_RECURRING
     * SCHEDULED_CUSTOM_PLAN
     */
    @NotNull(message = "Order type is required")
    private String orderType;

    // ================= SCHEDULED =================

    private LocalDateTime scheduledDeliveryDateTime;

    private LocalDateTime subscriptionStartDate;

    private LocalDateTime subscriptionEndDate;

    /**
     * BREAKFAST
     * LUNCH
     * DINNER
     */
    private String mealPreference;

    private List<CoOrderItemDto> items;

    // ================= CUSTOM PLAN =================

    private List<CoScheduledOrderDto> scheduledOrders;

    // ================= AUDIT =================

    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ================= GROUP ORDER =================

    private Integer groupOrderInvitationId;

    // ================= ORDER =================

    private String orderId;

    private String orderStatus;

    // ================= INSTRUCTIONS =================

    private String cookingInstructions;

    private Boolean isCutleryRequired;
}