package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class GroupOrderEventDto {

    private String eventType; // e.g., "MEMBER_JOINED", "MEMBER_LEFT"
    private Integer groupOrdersInvitationId;
    private Integer customerId;
    private String customerName;
    private Integer deliveryAddressId;
}
