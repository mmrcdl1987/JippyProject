package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmAssignCancelledOrderRequestDto;
import com.jippy.foodandmart.dto.FmAssignCancelledOrderResponseDto;

public interface FmCancelledOrdersInventoryService {

    FmAssignCancelledOrderResponseDto
    assignCancelledOrder(
            FmAssignCancelledOrderRequestDto requestDto);
}