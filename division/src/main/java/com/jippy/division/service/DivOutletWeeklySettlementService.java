package com.jippy.division.service;

import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;

public interface DivOutletWeeklySettlementService {
    DivOutletWeeklySettlementResponseDto getOutletWeeklySettlement(Integer weeklySettlementId);

//    void sendOutletSettlementMail(Integer weeklySettlementId);

    void sendOutletSettlementMail();
}
