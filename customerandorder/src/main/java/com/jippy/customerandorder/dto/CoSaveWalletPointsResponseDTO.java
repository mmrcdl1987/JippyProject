package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CoSaveWalletPointsResponseDTO {

    @Schema(
            example = "ORD012",
            description = "Order ID for which wallet points are being saved"
    )
    private String orderId;

    @Schema(
            example = "112",
            description = "Customer ID associated with the delivered order"
    )
    private Integer customerId;

    @Schema(
            example = "14",
            description = "Wallet ID associated with the customer"
    )
    private Integer walletId;

    @Schema(
            example = "400.00",
            description = "Discounted order amount used to calculate and add wallet points"
    )
    private BigDecimal orderAmountDiscounted;

    @Schema(
            example = "2500",
            description = "Wallet balance points before adding the discounted order amount"
    )
    private Integer previousBalancePoints;

    @Schema(
            example = "2900",
            description = "Updated wallet balance points after adding the discounted order amount"
    )
    private Integer updatedBalancePoints;

    @Schema(
            example = "2900",
            description = "Points recorded in the wallet transaction history after updating the wallet balance"
    )
    private Integer transactionPoints;

    @Schema(
            example = "ORDER_VALUE_POINTS",
            description = "Type of wallet points transaction generated from the order value"
    )
    private String pointsType;

    @Schema(
            example = "SUCCESS",
            description = "Status of the wallet points operation"
    )
    private String status;

    @Schema(
            example = "Wallet points saved successfully equivalent to order_amount_discounted",
            description = "Response message indicating the result of the wallet points operation"
    )
    private String message;
}