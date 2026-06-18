package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.OutletSubscriptionResponseDto;

public interface OutletSubscriptionPlanService {

    OutletSubscriptionResponseDto getOutletSubscription(Integer outletId);
}