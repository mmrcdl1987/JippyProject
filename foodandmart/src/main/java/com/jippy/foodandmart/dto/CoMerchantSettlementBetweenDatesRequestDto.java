package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CoMerchantSettlementBetweenDatesRequestDto {

    private List<Integer> outletIds;

    private LocalDate startDate;

    private LocalDate endDate;
}