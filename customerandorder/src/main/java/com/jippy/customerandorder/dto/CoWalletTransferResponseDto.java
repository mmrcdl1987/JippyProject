package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoWalletTransferResponseDto {

    private Boolean success;

    private String message;

    private Integer senderCustomerId;

    private Integer receiverCustomerId;

    private Integer transferredPoints;

    private Integer senderRemainingPoints;
}