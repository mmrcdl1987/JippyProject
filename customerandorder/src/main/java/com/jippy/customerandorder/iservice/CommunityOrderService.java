package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoAddOrDropMembersFromCommunityDto;
import com.jippy.customerandorder.dto.CoCommunityEventsDto;
import com.jippy.customerandorder.dto.CoResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface CommunityOrderService {

    ResponseEntity<CoResponseDto> createEvents(@Valid CoCommunityEventsDto coCommunityEventsDto);

    ResponseEntity<CoResponseDto> AddOrDropMembersFromCommunity(@Valid CoAddOrDropMembersFromCommunityDto addOrDropMembersFromCommunityDto);

    ResponseEntity<Integer> findCustomerInCommunity(@Valid Double latitude, Double longitude);
}
