package com.jippy.division.mapper;


import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.entity.DivOutletWeeklySettlement;
import org.springframework.stereotype.Component;

@Component
public class DivOutletWeeklySettlementMapper {

    public DivOutletWeeklySettlementResponseDto convertToResponseDto(DivOutletWeeklySettlement settlement) {

        DivOutletWeeklySettlementResponseDto responseDto = new DivOutletWeeklySettlementResponseDto();

        responseDto.setWeeklySettlementId(settlement.getWeeklySettlementId());

        responseDto.setOutletId(settlement.getOutletId());

        responseDto.setWeekStartDate(settlement.getWeekStartDate());

        responseDto.setWeekEndDate(settlement.getWeekEndDate());

        responseDto.setTotalSettlementAmount(settlement.getTotalSettlementAmount());

        responseDto.setPaymentStatus(settlement.getPaymentStatus());

        responseDto.setTransactionId(settlement.getTransactionId());

        responseDto.setOutletMobileNumber(settlement.getOutletMobileNumber());

        responseDto.setOutletEmail(settlement.getOutletEmail());

        responseDto.setOrdersCount(settlement.getOrdersCount());

        responseDto.setDeductions(settlement.getDeductions());

        return responseDto;
    }
}