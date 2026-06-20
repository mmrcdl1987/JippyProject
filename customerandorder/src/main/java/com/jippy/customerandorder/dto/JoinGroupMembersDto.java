package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinGroupMembersDto {

    @NotNull(message = "Group Orders Invitation ID cannot be null")
    private Integer groupOrdersInvitationId;

    @NotNull(message = "Customer ID cannot be null")
    private Integer customerId;

    @NotNull(message = "Delivery Address ID cannot be null")
    private Integer deliveryAddressId;

    @NotNull(message = "Invitation code cannot be null")
    private String invitationCode;

    private LocalDateTime createdAt;

    @NotNull(message = "Created By cannot be null")
    private Integer createdBy;

    private boolean isDropped;

}
