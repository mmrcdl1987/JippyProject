package com.jippy.division.mapper;

import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.entity.DivOutletWeeklySettlement;
import org.springframework.stereotype.Component;

@Component
public class DivOutletWeeklySettlementMapper {

    public DivOutletWeeklySettlementResponseDto convertToResponseDto(
            DivOutletWeeklySettlement settlement) {

        DivOutletWeeklySettlementResponseDto dto =
                new DivOutletWeeklySettlementResponseDto();

        dto.setWeeklySettlementId(settlement.getWeeklySettlementId());

        dto.setOutletId(settlement.getOutletId());

        dto.setWeekStartDate(settlement.getWeekStartDate());

        dto.setWeekEndDate(settlement.getWeekEndDate());

        dto.setTotalSettlementAmount(settlement.getTotalSettlementAmount());

        dto.setPaymentStatus(settlement.getPaymentStatus());

        dto.setTransactionId(settlement.getTransactionId());

        dto.setOutletMobileNumber(settlement.getOutletMobileNumber());

        dto.setOutletEmail(settlement.getOutletEmail());

        dto.setOrdersCount(settlement.getOrdersCount());

        dto.setDeductions(settlement.getDeductions());

        dto.setGst(settlement.getGst());

        dto.setPromotionAmount(settlement.getPromotionAmount());

        dto.setSubscriptionAmount(settlement.getSubscriptionAmount());

        dto.setNetSettlementAmount(settlement.getNetSettlementAmount());

        return dto;
    }
}