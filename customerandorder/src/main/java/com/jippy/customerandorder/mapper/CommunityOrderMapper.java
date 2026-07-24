package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoAddOrDropMembersFromCommunityDto;
import com.jippy.customerandorder.dto.CoCommunityEventsDto;
import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class CommunityOrderMapper {

    public static CoCommunityEvents toEntity(CoCommunityEventsDto communityEventsDto){

        CoCommunityEvents communityEvents = new CoCommunityEvents();

        communityEvents.setCommunityId(communityEventsDto.getCommunityId());
        communityEvents.setCreatedBy(communityEventsDto.getCreatedBy());
        communityEvents.setCreatedAt(LocalDateTime.now());
        communityEvents.setEventTitle(communityEventsDto.getEventTitle());
        communityEvents.setEventDescription(communityEventsDto.getEventDescription());
        communityEvents.setEventStartDate(communityEventsDto.getEventStartDate());
        communityEvents.setEventEndDate(communityEventsDto.getEventEndDate());
        communityEvents.setBookingStartDate(communityEventsDto.getBookingStartDate());
        communityEvents.setBookingEndDate(communityEventsDto.getBookingEndDate());
        communityEvents.setLocationName(communityEventsDto.getLocationName());
        communityEvents.setDeliveryTime(communityEventsDto.getDeliveryTime());
        communityEvents.setCreatedBy(communityEventsDto.getCreatedBy());
        communityEvents.setOutletId(communityEventsDto.getOutletId());
        communityEvents.setMaxMembers(communityEventsDto.getMaxMembers());

        return communityEvents;

    }

    public static CoCustomerCommunities toCustomerCommunity(CoAddOrDropMembersFromCommunityDto addOrDropMembersFromCommunityDto) {

        CoCustomerCommunities customerCommunities = new CoCustomerCommunities();

        customerCommunities.setCommunityId(addOrDropMembersFromCommunityDto.getCommunityId());
        customerCommunities.setCustomerId(addOrDropMembersFromCommunityDto.getCustomerId());
        customerCommunities.setCreatedAt(LocalDateTime.now());
        customerCommunities.setCreatedBy(addOrDropMembersFromCommunityDto.getCustomerId());

        return customerCommunities;
    }
}
