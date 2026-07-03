package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoWalletTransactionHistoryDto {

    private String pointsType;
    private Integer points;
    private LocalDateTime createdAt;

}