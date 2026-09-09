package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(
        name = "CoOrderDetailsOfOutletDto",
        description = "Order details for an outlet with merchant total price"
)
public class CoOrderDetailsOfOutletDto {

    @Schema(description = "Unique order ID", example = "jippy202609052")
    private String orderId;

    @Schema(description = "Outlet ID", example = "13")
    private Integer outletId;

    @Schema(description = "Outlet name", example = "jippy ongole ap 123")
    private String outletName;

    @Schema(description = "Customer name", example = "Rama Krishna")
    private String customerName;

    @Schema(description = "Driver ID", example = "16")
    private Integer driverId;

    @Schema(description = "Driver name", example = "Prathyusha thatikonda")
    private String driverName;

    @Schema(description = "Driver mobile number", example = "@#$%^&")
    private String driverMobileNumber;

    @Schema(description = "Current order status", example = "ORDER_CONFIRMED")
    private String orderStatus;

    @Schema(
            description = "Total merchant price calculated from order items",
            example = "160.00"
    )
    private BigDecimal merchantTotalPrice;

    @Schema(description = "Area name", example = "Ameerpet")
    private String areaName;
}