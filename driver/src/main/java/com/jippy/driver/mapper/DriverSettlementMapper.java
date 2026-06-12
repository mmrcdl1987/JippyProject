package com.jippy.driver.mapper;

import com.jippy.driver.dto.DriverIncentiveDetailDto;
import com.jippy.driver.dto.DriverIncentiveSettlementResponseDto;
import com.jippy.driver.dto.DriverOrderSettlementDto;
import com.jippy.driver.dto.DriverSettlementResponseDto;
import com.jippy.driver.projection.DriverIncentiveDetailProjection;
import com.jippy.driver.projection.DriverIncentiveSettlementProjection;
import com.jippy.driver.projection.DriverOrderSettlementProjection;
import com.jippy.driver.projection.DriverSettlementProjection;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DriverSettlementMapper {

    //     for api "/getDriversSettlements"
    // for api response, we need to return total earnings along with the details of
// each order, so we will use this method to map the projection to response dto
    public static DriverSettlementResponseDto toDriverSettlementResponseDto(DriverSettlementProjection projection) {

        DriverSettlementResponseDto dto = new DriverSettlementResponseDto();

        dto.setDriverId(projection.getDriverId());

        dto.setNoOfOrdersCompleted(projection.getNoOfOrdersCompleted());

        dto.setTotalDriverEarnings(projection.getTotalDriverEarnings());

        return dto;
    }

    public static DriverOrderSettlementDto toDriverOrderSettlementDto(DriverOrderSettlementProjection projection) {

        DriverOrderSettlementDto dto = new DriverOrderSettlementDto();

        dto.setOrderId(projection.getOrderId());

        dto.setPickUpCharges(projection.getPickUpCharges());

        dto.setDeliverCharges(projection.getDeliverCharges());

        dto.setTotalDeliveryFee(projection.getTotalDeliveryFee());

        dto.setSurgeFee(projection.getSurgeFee());

        dto.setTips(projection.getTips());

        return dto;
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------
//    for api "/getDriversIncentivesSettlements"
    // for api response, we need to return total incentives amount along with the details of each
    // incentive, so we will use this method to map the projection to response dto
    public static DriverIncentiveSettlementResponseDto toDriverIncentiveSettlementResponseDto(DriverIncentiveSettlementProjection projection) {

        DriverIncentiveSettlementResponseDto dto = new DriverIncentiveSettlementResponseDto();

        dto.setDriverId(projection.getDriverId());

        dto.setTotalIncentivesAmount(projection.getTotalIncentivesAmount());

        return dto;
    }

    public static DriverIncentiveDetailDto toDriverIncentiveDetailDto(DriverIncentiveDetailProjection projection) {

        DriverIncentiveDetailDto dto = new DriverIncentiveDetailDto();

        dto.setCurrDate(projection.getCurrDate());

        dto.setIncentiveAmount(projection.getIncentiveAmount());

        dto.setCompletedOrdersCount(projection.getCompletedOrdersCount());

        return dto;
    }
}
