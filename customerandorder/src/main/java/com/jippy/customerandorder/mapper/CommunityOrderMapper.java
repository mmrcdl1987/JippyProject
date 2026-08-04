package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoAddOrDropMembersFromCommunityDto;
import com.jippy.customerandorder.dto.CoCommunityEventsDto;
import com.jippy.customerandorder.dto.CommunityDto;
import com.jippy.customerandorder.entity.CoCommunity;
import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;
import org.locationtech.jts.geom.MultiPolygon;
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

        return community;
    }
}
