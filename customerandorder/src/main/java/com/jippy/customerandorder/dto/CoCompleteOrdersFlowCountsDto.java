package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "CoCompleteOrdersFlowCountsDto",
        description = "Contains complete order flow counts based on order status"
)
public class CoCompleteOrdersFlowCountsDto {

    @Schema(
            description = "Total number of orders",
            example = "7"
    )
    private Long totalOrdersCount;

    @Schema(
            description = "Number of orders with ORDER_PLACED status",
            example = "2"
    )
    private Long ordersPlaced;

    @Schema(
            description = "Number of orders with ORDER_CONFIRMED status",
            example = "2"
    )
    private Long ordersConfirmed;

    @Schema(
            description = "Number of orders with ORDER_SHIPPED status",
            example = "2"
    )
    private Long ordersShipped;

    @Schema(
            description = "Number of orders with ORDER_COMPLETED status",
            example = "1"
    )
    private Long ordersCompleted;

    @Schema(
            description = "Number of orders with ORDER_REJECTED status",
            example = "2"
    )
    private Long ordersRejected;
}