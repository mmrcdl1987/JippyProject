package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceGroupOrderRequestDto {

    @NotNull(message = "Group order Invitation id is not null")
    private Integer groupOrderInvitationId;

    @NotNull(message = "Payment mode id is not null")
    private Integer paymentModeId;

    @NotNull(message = "Customer id is not null")
    private Integer hostCustomerId;

}
