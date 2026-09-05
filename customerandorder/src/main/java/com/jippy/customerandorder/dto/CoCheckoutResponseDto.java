package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoCheckoutResponseDto {

    private Integer outletId;

    private List<CoCartItemResponseDto> items;

    // ================= ORDER =================

    private BigDecimal itemTotal;

    private BigDecimal orderAmountDiscounted;

    // ================= DRIVER DELIVERY =================

    /**
     * Delivery charge calculated by Driver service.
     * This is informational/internal and is NOT included
     * in customer payable amount.
     */
    private BigDecimal driverDeliveryCharge;

    // ================= CUSTOMER DELIVERY =================

    private BigDecimal deliveryDistanceKm;
    /**
     * Customer delivery charge before free-distance benefit.
     */
    private BigDecimal customerGrossDeliveryCharge;

    /**
     * Amount saved because of free delivery distance.
     */
    private BigDecimal customerFreeDistanceBenefit;

    /**
     * Final customer delivery charge after free-distance benefit.
     */
    private BigDecimal customerDeliveryCharge;

    /**
     * GST calculated on customerGrossDeliveryCharge.
     */
    private BigDecimal customerDeliveryTax;

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

    // ================= TAXES =================

    private BigDecimal foodTax;

    /**
     * Total GST/taxes applicable to the customer checkout.
     *
     * Includes:
     * - Food GST
     * - Customer delivery GST
     * - Platform fee GST
     * - Surge fee GST
     * - Packaging fee GST
     */
    private BigDecimal taxesAndCharges;

    // ================= DISCOUNT / TIP =================

    private BigDecimal couponDiscount;

    private BigDecimal deliveryTip;

    // ================= FINAL =================

    private BigDecimal toPay;

    private Boolean codAvailable;
}