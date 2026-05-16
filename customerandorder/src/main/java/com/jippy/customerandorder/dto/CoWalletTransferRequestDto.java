package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoWalletTransferRequestDto {

    private Integer senderCustomerId;

    private String receiverPhoneNumber;

    private Integer transferPoints;

    private Integer createdBy;
}