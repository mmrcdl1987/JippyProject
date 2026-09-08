package com.jippy.division.mapper;

import com.jippy.division.dto.DivRefundDetailsDto;
import com.jippy.division.projection.DivRefundDetailsProjection;

import java.math.BigDecimal;

public class DivRefundMapper {

    private DivRefundMapper() {
        // Utility class
    }

    /**
     * Converts refund amount from paise to rupees.
     * <p>
     * Example:
     * 45443 paise = 454.43 rupees
     */
    public static DivRefundDetailsDto mapToDto(DivRefundDetailsProjection projection) {

        DivRefundDetailsDto dto = new DivRefundDetailsDto();

        dto.setApplicationOrderId(projection.getApplicationOrderId());

        dto.setPaymentTransactionsId(projection.getPaymentTransactionsId());

        BigDecimal amountInPaise = projection.getAmountInPaise();

        if (amountInPaise != null) {

            dto.setAmountInRupees(amountInPaise.divide(BigDecimal.valueOf(100)));
        }

        dto.setRefundStatus(projection.getRefundStatus());

        dto.setReason(projection.getReason());

        dto.setCreatedAt(projection.getCreatedAt());

        return dto;
    }
}