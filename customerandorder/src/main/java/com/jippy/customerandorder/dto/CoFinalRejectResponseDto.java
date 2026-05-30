package com.jippy.customerandorder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CoFinalRejectResponseDto {

    private Boolean success;

    private String message;

    private String orderStatus;

    private Integer totalOutlets;

    private List<CoOutletDto> outlets;
}