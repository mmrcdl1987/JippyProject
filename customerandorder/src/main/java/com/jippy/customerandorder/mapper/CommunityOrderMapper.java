package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCommunity;
import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;
import com.jippy.customerandorder.projection.CoActiveCommunityGroupOrdersProjection;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

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

    public static CoCommunity mapToCommunityEntity(CoCommunity communityToUpdate, CommunityDto communityDto, Polygon polygon) {

        CoCommunity community = new CoCommunity();

        if(communityDto.getCommunityId() != null) {
            community.setCommunityId(communityDto.getCommunityId());
            community.setUpdatedAt(LocalDateTime.now());
            community.setUpdatedBy(communityDto.getUpdatedBy());
        }
        community.setCommunityName(communityDto.getCommunityName());
        community.setCommunityAreaId(communityDto.getCommunityAreaId());
        community.setBoundary(polygon);
        community.setAboutCommunity(communityDto.getAboutCommunity());
        community.setEstablishedYear(communityDto.getEstablishedYear());
        community.setCreatedBy(communityDto.getCreatedBy());
        community.setCreatedAt(LocalDateTime.now());
        community.setNoOfFamilies(communityDto.getNoOfFamilies());

        return community;
    }

    public static CoCommunityResponseDto toCommunityResponseDto(CoCommunity community, String areaName) {

        CoCommunityResponseDto communityResponseDto = new CoCommunityResponseDto();

        communityResponseDto.setCommunityId(community.getCommunityId());
        communityResponseDto.setCommunityName(community.getCommunityName());
        communityResponseDto.setCommunityAreaName(areaName);
        communityResponseDto.setAboutCommunity(community.getAboutCommunity());
        communityResponseDto.setEstablishedYear(community.getEstablishedYear());
        communityResponseDto.setCommunityImageUrl(community.getCommunityImageUrl());
        communityResponseDto.setNoOfFamilies(community.getNoOfFamilies());

        return communityResponseDto;
    }

    public static CoActiveGroupOrdersResponseDto toCommunityOrdersResponseDto(CoActiveCommunityGroupOrdersProjection groupOrdersProjection) {

        CoActiveGroupOrdersResponseDto groupOrdersResponseDto = new CoActiveGroupOrdersResponseDto();

        groupOrdersResponseDto.setActiveOrdersCount(groupOrdersProjection.getActiveOrdersCount());
        groupOrdersResponseDto.setCommunityId(groupOrdersProjection.getCommunityId());
        groupOrdersResponseDto.setGroupOrdersInvitationId(groupOrdersProjection.getGroupOrdersInvitationId());
        groupOrdersResponseDto.setMaxMembers(groupOrdersProjection.getMaxMembers());
        groupOrdersResponseDto.setOrderClosingTimeInMinutes(groupOrdersProjection.getOrderClosingTimeInMinutes());

        return groupOrdersResponseDto;

    }
}
