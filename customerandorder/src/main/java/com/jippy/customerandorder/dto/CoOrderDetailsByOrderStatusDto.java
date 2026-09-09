package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO for fetching complete order details
 * based on order status.
 */
@Getter
@Setter
@Schema(
        name = "CoOrderDetailsByOrderStatusDto",
        description = "Complete order details filtered by order status"
)
public class CoOrderDetailsByOrderStatusDto {

    @Schema(
            description = "Unique order ID",
            example = "jippy202609014"
    )
    private String orderId;

    @Schema(
            description = "Outlet ID associated with the order",
            example = "13"
    )
    private Integer outletId;

    @Schema(
            description = "Outlet name",
            example = "Dosa Corner"
    )
    private String outletName;

    @Schema(
            description = "Customer full name",
            example = "Rahul Reddy"
    )
    private String customerName;

    @Schema(
            description = "Driver ID assigned to the order",
            example = "15"
    )
    private Integer driverId;
    @Schema(
            description = "Driver full name",
            example = "Rohan Kumar"
    )
    private String driverName;
    @Schema(
            description = "Driver Mobile Number",
            example = "6305123408"
    )
    private String driverMobileNumber;

    @Schema(
            description = "Current order status",
            example = "ORDER_PLACED"
    )
    private String orderStatus;

    @Schema(
            description = "Total order amount",
            example = "454.43"
    )
    private BigDecimal orderAmount;

    @Schema(
            description = "Area name where the outlet is located",
            example = "Kukatpally"
    )
    private String areaName;

}