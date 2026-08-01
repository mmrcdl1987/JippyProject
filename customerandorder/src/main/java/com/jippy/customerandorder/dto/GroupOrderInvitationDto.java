package com.jippy.customerandorder.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupOrderInvitationDto {

    private Integer groupOrdersInvitationId;

    @NotNull(message = "Host customer ID cannot be null")
    private Integer hostCustomerId;

    @NotNull(message = "Outlet ID cannot be null")
    private Integer outletId;

    private String invitationCode;
    private String status;

    @NotNull(message = "Expiration time cannot be null")
    @Min(value = 15, message = "A group must have at least 15 minutes as order closing time")
    @Max(value = 60, message = "A group cannot have more than 60 minutes as order closing time")
    private  Integer orderCloseDurationInMinutes;

    @NotNull(message = "Payment responsibility cannot be null")
    private String paymentResponsibility;

    @NotNull(message = "Max members cannot be null")
    @Min(value = 2, message = "A group must have at least 2 members")
    @Max(value = 10, message = "A group cannot have more than 10 members")
    private Integer maxMembers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer createdBy;

    private String orderType;

    private Integer communityEventId;

    private Integer communityId;

    private String  webSocketEndPoint;

    private String webSocketTopic;

}
