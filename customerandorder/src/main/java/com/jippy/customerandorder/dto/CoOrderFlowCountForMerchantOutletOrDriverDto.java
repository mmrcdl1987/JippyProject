package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO containing order flow counts
 * for a merchant or a specific outlet.
 */
@Getter
@Setter
@Schema(
        name = "CoOrderFlowCountForMerchantOrOutletDto",
        description = "Contains total, completed and rejected order counts"
)
public class CoOrderFlowCountForMerchantOutletOrDriverDto  {

    @Schema(
            description = "Total number of orders",
            example = "11"
    )
    private Long totalOrdersCount;

    @Schema(
            description = "Number of completed orders",
            example = "1"
    )
    private Long completedOrdersCount;

    @Schema(
            description = "Number of rejected orders",
            example = "2"
    )
    private Long rejectedOrdersCount;
}