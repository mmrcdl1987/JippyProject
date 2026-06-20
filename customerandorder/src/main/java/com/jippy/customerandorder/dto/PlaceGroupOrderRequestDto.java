package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class PlaceGroupOrderRequestDto {

    private Integer groupOrderInvitationId;
    private Integer paymentModeId;
    private Integer hostCustomerId;

}
