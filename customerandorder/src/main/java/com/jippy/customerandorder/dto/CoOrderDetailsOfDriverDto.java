package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(
        name = "CoOrderDetailsOfDriverDto",
        description = "Order details assigned to a driver with pickup, delivery distance and driver charges"
)
public class CoOrderDetailsOfDriverDto {

    @Schema(
            description = "Unique order ID",
            example = "jippy202609031"
    )
    private String orderId;

    @Schema(
            description = "Outlet ID associated with the order",
            example = "13"
    )
    private Integer outletId;

    @Schema(
            description = "Outlet name",
            example = "jippy ongole ap 123"
    )
    private String outletName;

    @Schema(
            description = "Customer name",
            example = "Mahesh Kumar"
    )
    private String customerName;

    @Schema(
            description = "Driver ID assigned to the order",
            example = "15"
    )
    private Integer driverId;

    @Schema(
            description = "Driver name",
            example = "tejashwini Yelisetty"
    )
    private String driverName;

    @Schema(
            description = "Driver mobile number",
            example = "9876543210"
    )
    private String driverMobileNumber;

    @Schema(
            description = "Current order status",
            example = "ORDER_SHIPPED"
    )
    private String orderStatus;

    @Schema(
            description = "Distance from driver to outlet in kilometers",
            example = "3.00"
    )
    private BigDecimal pickUpDistanceInKms;

    @Schema(
            description = "Distance from outlet to customer in kilometers",
            example = "5.50"
    )
    private BigDecimal deliveryDistanceInKms;

    @Schema(
            description = "Charges for picking up the order",
            example = "20.00"
    )
    private BigDecimal pickUpCharges;

    @Schema(
            description = "Driver delivery fee",
            example = "50.00"
    )
    private BigDecimal driverDeliveryFee;

    @Schema(
            description = "Total driver charges calculated as pickup charges plus driver delivery fee",
            example = "70.00"
    )
    private BigDecimal driverTotalCharges;

    @Schema(
            description = "Area name where the outlet is located",
            example = "Ameerpet"
    )
    private String areaName;
}