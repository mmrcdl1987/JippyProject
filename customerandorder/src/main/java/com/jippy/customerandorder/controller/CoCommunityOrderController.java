package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.iservice.CommunityOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/co/community-order")
public class CoCommunityOrderController {

    private final CommunityOrderService communityOrderService;

    @PostMapping("/createEvents")
    @Operation(summary = "Create Community events", description = "All dates like event start and end dates, booking start and end dates, delivery time all should be in this format,  ")
    public ResponseEntity<CoResponseDto> createEvents(@Valid @RequestBody
    CoCommunityEventsDto coCommunityEventsDto) {

        log.info("Create Community events API called with the json : {} ", coCommunityEventsDto);

        return communityOrderService.createEvents(coCommunityEventsDto);
    }

    @PostMapping("/AddOrDropMembersFromCommunity")
    @Operation(summary = "Add Or Drop Members From Community", description = "While adding members give type as 'JOIN' and give type as 'DROP' to remove  ")
    public ResponseEntity<CoResponseDto> AddOrDropMembersFromCommunity(@Valid @RequestBody
            CoAddOrDropMembersFromCommunityDto addOrDropMembersFromCommunityDto) {

        log.info("Add/Drop member from community :{} ", addOrDropMembersFromCommunityDto);

        return communityOrderService.AddOrDropMembersFromCommunity(addOrDropMembersFromCommunityDto);
    }

    @GetMapping("/findCustomerInCommunity")
    @Operation(summary = "Find Customer In Community", description = "Checks given latitude and longitude with all communities, if matches return corresponding community Id, else 0  ")
    public ResponseEntity<Integer> findCustomerInCommunity(@Valid @RequestParam Double latitude,
    @RequestParam Double longitude) {

        log.info("Check Customer location In Community: {} ,{} ",latitude,longitude );

        return communityOrderService.findCustomerInCommunity(latitude,longitude);
    }

    @PostMapping(value = "/createCommunity",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create Communities", description = "Create Communities")
    public ResponseEntity<CoResponseDto> createCommunity(@RequestPart("communityDto") CommunityDto communityDto,
            @RequestPart(value = "communityImage", required = false) MultipartFile communityImage) {

        log.info("POST API called for created Community:");
        String message = communityOrderService.createCommunity(communityDto,communityImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CoResponseDto(COConstants.STATUS_201, message));
    }

}
