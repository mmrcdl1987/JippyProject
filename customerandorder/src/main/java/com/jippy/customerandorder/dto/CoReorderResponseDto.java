package com.jippy.customerandorder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class CoReorderResponseDto {

    private String orderId;

    private Integer customerId;

    private Integer addedItemsCount;

    private Integer unavailableItemsCount;

    private List<String> unavailableItems;

    private String message;
}