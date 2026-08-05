package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoActiveGroupOrdersResponseDto {

    private Integer groupOrdersInvitationId;
    private Integer orderClosingTimeInMinutes;
    private Integer maxMembers;
    private Integer communityId;
    private Integer activeOrdersCount;
}
