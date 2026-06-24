package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.iservice.GroupOrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/group-orders")
@Slf4j
@AllArgsConstructor
public class GroupOrderController {

    private  final GroupOrderService groupOrderService;

    // CREATE Group Order Invitation
    @PostMapping("/createGroupOrderInvitation")
    public ResponseEntity<GroupOrderInvitationDto>  createGroupOrderInvitation(@Valid @RequestBody GroupOrderInvitationDto
                                                                                 groupCreationDto) {

        log.info("Create group order invitation request received: {}", groupCreationDto);

        return groupOrderService.createGroupOrderInvitation(groupCreationDto);
    }

    @PostMapping("/joinGroupMembers")
    public ResponseEntity<CoResponseDto>  joinGroupMembers(@Valid @RequestBody
                            JoinGroupMembersDto joinGroupMembersDto) {

        log.info("Join Group members request received: {}", joinGroupMembersDto);

        return groupOrderService.joinGroupMembers(joinGroupMembersDto);
    }


    @PostMapping("/addItemsToGroupCart")
    public ResponseEntity<CoResponseDto>  addItemsToGroupCart(@Valid @RequestBody
    CoGroupCartItemsDto groupCartItemsDto) {

        log.info("Add items to group cart request received: {}", groupCartItemsDto);

        return groupOrderService.addItemsToGroupCart(groupCartItemsDto);
    }

    @PostMapping("/groupOrderCheckOut")
    public ResponseEntity<GroupOrderCheckoutDto>  groupOrderCheckOut(@RequestParam Integer groupOrdersInvitationId,
            @RequestParam Integer hostCustomerId,@RequestParam(required = false) Double couponDiscount,
            @RequestParam(required = false) Double deliveryTip) {

         log.info("Group order checkout request received for groupOrdersInvitationId: {} and customerId: {}",
                 groupOrdersInvitationId, hostCustomerId);

        return groupOrderService.groupOrderCheckOut(groupOrdersInvitationId, hostCustomerId,
                couponDiscount,deliveryTip);
    }


    @PostMapping("/groupPaymentDetails")
    public ResponseEntity<GroupOrderPaymentDetailsResponseDto>  groupPaymentDetails(@RequestBody GroupPaymentDetailsDto
            groupPaymentDetailsDto) {

        log.info("Group payment details API Initialised for group order invitation Id: {}"
                ,groupPaymentDetailsDto.getGroupOrderInvitationId());

        return groupOrderService.groupPaymentDetails(groupPaymentDetailsDto);
    }


   @PostMapping("/placeGroupOrder")
    public ResponseEntity<CoResponseDto>  placeGroupOrder(@RequestBody PlaceGroupOrderRequestDto placeGroupOrderRequestDto) {

        log.info("Group order place order request received for groupOrdersInvitationId: {} and customerId: {}",
                placeGroupOrderRequestDto.getGroupOrderInvitationId(), placeGroupOrderRequestDto.getHostCustomerId());

        return groupOrderService.placeGroupOrder(placeGroupOrderRequestDto);
    }


}
