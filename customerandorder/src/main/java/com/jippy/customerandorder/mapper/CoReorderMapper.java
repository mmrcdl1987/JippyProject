package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoReorderResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CoReorderMapper {

    public CoReorderResponseDto mapToResponseDto(String orderId, Integer customerId, Integer addedItemsCount, List<String> unavailableItems, String message) {

        CoReorderResponseDto responseDto = new CoReorderResponseDto();

        responseDto.setOrderId(orderId);

        responseDto.setCustomerId(customerId);

        responseDto.setAddedItemsCount(addedItemsCount);

        responseDto.setUnavailableItemsCount(unavailableItems.size());

        responseDto.setUnavailableItems(unavailableItems);

        responseDto.setMessage(message);

        return responseDto;
    }
}