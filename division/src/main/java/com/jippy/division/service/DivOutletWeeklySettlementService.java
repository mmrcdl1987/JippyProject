package com.jippy.division.service;

import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.enums.DivSettlementFilter;

import java.util.List;

public interface DivOutletWeeklySettlementService {

    /**
     * Get settlement by settlement id
     */
    DivOutletWeeklySettlementResponseDto getOutletWeeklySettlement(Integer weeklySettlementId);

    /**
     * Get settlement history
     * Merchant -> All outlets
     * Outlet -> Single outlet
     */
    List<DivOutletWeeklySettlementResponseDto> getWeeklySettlements(
            Integer merchantId,
            Integer outletId,
            DivSettlementFilter filter);

    /**
     * Scheduler method
     */
    void sendOutletSettlementMail();
}