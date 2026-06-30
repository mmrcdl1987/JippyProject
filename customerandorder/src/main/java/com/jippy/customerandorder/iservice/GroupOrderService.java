package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface GroupOrderService {

    ResponseEntity<?> createGroupOrderInvitation(GroupOrderInvitationDto groupCreationDto);

    ResponseEntity<CoResponseDto> joinGroupMembers(
            @Valid JoinGroupMembersDto joinGroupMembersDto);

    ResponseEntity<CoResponseDto> addItemsToGroupCart(@Valid CoGroupCartItemsDto groupCartItemsDto);

    ResponseEntity<GroupOrderCheckoutDto> groupOrderCheckOut(Integer groupOrdersInvitationId,
            Integer hostCustomerId, Double couponDiscount, Double deliveryTip
    );

    ResponseEntity<GroupOrderPaymentDetailsResponseDto> groupPaymentDetails(GroupPaymentDetailsDto groupPaymentDetailsDto);

    ResponseEntity<CoResponseDto> placeGroupOrder(PlaceGroupOrderRequestDto placeGroupOrderRequestDto);

    ResponseEntity<?> getActiveGroupOrder(Integer hostCustomerId);
}
