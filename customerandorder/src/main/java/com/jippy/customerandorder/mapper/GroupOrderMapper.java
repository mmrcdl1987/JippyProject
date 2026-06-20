package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.GroupOrderInvitationDto;
import com.jippy.customerandorder.dto.JoinGroupMembersDto;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.GroupOrderInvitation;
import com.jippy.customerandorder.entity.GroupOrderMembers;

import java.time.LocalDateTime;

public class GroupOrderMapper {

    public static GroupOrderInvitation toGroupOrderInvitation(GroupOrderInvitationDto groupCreationDto, CoCustomer customer, String groupInvitationLink) {

        GroupOrderInvitation groupOrderInvitation = new GroupOrderInvitation();

        groupOrderInvitation.setCustomer(customer);
        groupOrderInvitation.setOutletId(groupCreationDto.getOutletId());
        groupOrderInvitation.setOrderClosingTimeInMinutes(groupCreationDto.getOrderCloseDurationInMinutes());
        groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_INVITATION_CREATED);
        groupOrderInvitation.setMaxMembers(groupCreationDto.getMaxMembers());
        groupOrderInvitation.setPaymentResponsibility(groupCreationDto.getPaymentResponsibility());
        groupOrderInvitation.setCreatedAt(LocalDateTime.now());
        groupOrderInvitation.setCreatedBy(customer.getCustomerId());
        groupOrderInvitation.setInvitationCode(groupInvitationLink);

        return groupOrderInvitation;
    }

    public static GroupOrderInvitationDto toGroupOrderInvitationResponseDto(GroupOrderInvitation savedGroupOrderInvitation) {

        GroupOrderInvitationDto groupOrderInvitationDto = new GroupOrderInvitationDto();

        groupOrderInvitationDto.setGroupOrdersInvitationId(savedGroupOrderInvitation.getGroupOrdersInvitationId());
        groupOrderInvitationDto.setHostCustomerId(savedGroupOrderInvitation.getCustomer().getCustomerId());
        groupOrderInvitationDto.setOutletId(savedGroupOrderInvitation.getOutletId());
        groupOrderInvitationDto.setOrderCloseDurationInMinutes(savedGroupOrderInvitation.getOrderClosingTimeInMinutes());
        groupOrderInvitationDto.setMaxMembers(savedGroupOrderInvitation.getMaxMembers());
        groupOrderInvitationDto.setPaymentResponsibility(savedGroupOrderInvitation.getPaymentResponsibility());
        groupOrderInvitationDto.setStatus(savedGroupOrderInvitation.getStatus());
        groupOrderInvitationDto.setInvitationCode(savedGroupOrderInvitation.getInvitationCode());
        groupOrderInvitationDto.setCreatedAt(savedGroupOrderInvitation.getCreatedAt());
        groupOrderInvitationDto.setCreatedBy(savedGroupOrderInvitation.getCreatedBy());

        return groupOrderInvitationDto;
    }

    public static GroupOrderMembers toGroupOrderMembersEntity(JoinGroupMembersDto joinGroupMembersDto,
                                                              GroupOrderInvitation groupOrderInvitation,
                                                              CoCustomer customer) {

        GroupOrderMembers groupOrderMembers = new GroupOrderMembers();

        groupOrderMembers.setGroupOrdersInvitation(groupOrderInvitation);
        groupOrderMembers.setCustomer(customer);
        groupOrderMembers.setDeliveryAddressId(joinGroupMembersDto.getDeliveryAddressId());
        groupOrderMembers.setOrderPlaced(COConstants.GROUP_ORDER_PLACED_FALSE);
        groupOrderMembers.setCreatedAt(LocalDateTime.now());
        groupOrderMembers.setCreatedBy(joinGroupMembersDto.getCustomerId());
        groupOrderMembers.setDropped(joinGroupMembersDto.isDropped());

        return groupOrderMembers;
    }
}
