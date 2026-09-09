package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentHashRequestDto {

    private String txnid;
    private String amount;
    private String productinfo;
    private String customerName;
    private String email;

}
