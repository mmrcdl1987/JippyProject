package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoAddOrDropMembersFromCommunityDto;
import com.jippy.customerandorder.dto.CoCommunityEventsDto;
import com.jippy.customerandorder.dto.CoResponseDto;
import com.jippy.customerandorder.dto.CommunityDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CommunityOrderService {

    ResponseEntity<CoResponseDto> createEvents(@Valid CoCommunityEventsDto coCommunityEventsDto);

    ResponseEntity<CoResponseDto> AddOrDropMembersFromCommunity(@Valid CoAddOrDropMembersFromCommunityDto addOrDropMembersFromCommunityDto);

    ResponseEntity<?> findCustomerInCommunity(@Valid Double latitude, Double longitude);

    String createCommunity(CommunityDto communityDto, MultipartFile communityImage);

    ResponseEntity<?> getActiveCommunityGroupOrders(Integer communityId);
}
