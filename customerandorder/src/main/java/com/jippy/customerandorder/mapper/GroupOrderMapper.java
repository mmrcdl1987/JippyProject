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

        //groupOrderInvitation.setHostCustomerId(customer.getCustomerId());
        groupOrderInvitation.setOutletId(groupCreationDto.getOutletId());
        groupOrderInvitation.setOrderClosingTimeInMinutes(groupCreationDto.getOrderCloseDurationInMinutes());
        groupOrderInvitation.setMaxMembers(groupCreationDto.getMaxMembers());
        groupOrderInvitation.setPaymentResponsibility(groupCreationDto.getPaymentResponsibility());
        groupOrderInvitation.setCreatedAt(LocalDateTime.now());
        groupOrderInvitation.setCreatedBy(customer.getCustomerId());
        groupOrderInvitation.setInvitationCode(groupInvitationLink);
        groupOrderInvitation.setOrderType(groupCreationDto.getOrderType());

        if(groupCreationDto.getOrderType().equals(COConstants.GROUP_ORDER_ORDER_TYPE) ){

            groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_INVITATION_ACTIVE);
            groupOrderInvitation.setHostCustomerId(groupCreationDto.getHostCustomerId());

        } else if(groupCreationDto.getOrderType().equals(COConstants.COMMUNITY_GROUP_ORDER_ORDER_TYPE)){

            groupOrderInvitation.setCommunityId(groupCreationDto.getCommunityId());
            groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_INVITATION_ACTIVE);
            groupOrderInvitation.setHostCustomerId(groupCreationDto.getHostCustomerId());

        }else {
            groupOrderInvitation.setCommunityEventId(groupCreationDto.getCommunityEventId());
            groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_INVITATION_CREATED);
        }

        return groupOrderInvitation;
    }

    public static GroupOrderInvitationDto toGroupOrderInvitationResponseDto(GroupOrderInvitation savedGroupOrderInvitation) {

        GroupOrderInvitationDto groupOrderInvitationDto = new GroupOrderInvitationDto();

        groupOrderInvitationDto.setGroupOrdersInvitationId(savedGroupOrderInvitation.getGroupOrdersInvitationId());
        groupOrderInvitationDto.setHostCustomerId(savedGroupOrderInvitation.getHostCustomerId());
        groupOrderInvitationDto.setOutletId(savedGroupOrderInvitation.getOutletId());
        groupOrderInvitationDto.setOrderCloseDurationInMinutes(savedGroupOrderInvitation.getOrderClosingTimeInMinutes());
        groupOrderInvitationDto.setMaxMembers(savedGroupOrderInvitation.getMaxMembers());
        groupOrderInvitationDto.setPaymentResponsibility(savedGroupOrderInvitation.getPaymentResponsibility());
        groupOrderInvitationDto.setStatus(savedGroupOrderInvitation.getStatus());
        groupOrderInvitationDto.setInvitationCode(savedGroupOrderInvitation.getInvitationCode());
        groupOrderInvitationDto.setCreatedAt(savedGroupOrderInvitation.getCreatedAt());
        groupOrderInvitationDto.setCreatedBy(savedGroupOrderInvitation.getCreatedBy());
        groupOrderInvitationDto.setOrderType(savedGroupOrderInvitation.getOrderType());

        // Construct the dynamic topic channel
        groupOrderInvitationDto.setWebSocketEndPoint(COConstants.WEB_SOCKET_END_POINT);
        groupOrderInvitationDto.setWebSocketTopic(COConstants.WEB_SOCKET_TOPIC+"/group-order/" + savedGroupOrderInvitation.getGroupOrdersInvitationId());

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
