package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoCustomerWalletResponseDto {

    private Integer walletId;
    private Integer customerId;
    private String customerName;
    private BigDecimal balanceAmount;
    private Integer balancePoints;

}
