package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;
import com.jippy.customerandorder.entity.GroupOrderInvitation;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.DriverFeignClient;
import com.jippy.customerandorder.iservice.CommunityOrderService;
import com.jippy.customerandorder.mapper.CommunityOrderMapper;
import com.jippy.customerandorder.mapper.GroupOrderMapper;
import com.jippy.customerandorder.repository.CoCommunityEventsRepository;
import com.jippy.customerandorder.repository.CoCustomerCommunityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityOrderServiceImpl implements CommunityOrderService {

    private final CoCommunityEventsRepository communityEventsRepository;
    private final DriverFeignClient driverFeignClient;
    private final CoCustomerCommunityRepository customerCommunityRepository;
    private final GroupOrderServiceImpl groupOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Transactional
    @Override
    public ResponseEntity<CoResponseDto> createEvents(CoCommunityEventsDto communityEventsDto) {

       Optional<CoCommunityEvents> communityEvents = communityEventsRepository.
               findByCommunityIdAndEventTitle(communityEventsDto.getCommunityId(),communityEventsDto.getEventTitle());
        if(communityEvents.isPresent()){
            log.warn("Already event exists with same event name and in the given community");
            return ResponseEntity.status(500).body(new CoResponseDto("500","Already event exists with same event name and in the given community "));
        }
        validateEventDates(communityEventsDto.getEventStartDate(),communityEventsDto.getEventEndDate(),
                communityEventsDto.getBookingStartDate(),communityEventsDto.getBookingEndDate());

        CoCommunityEvents coCommunityEvents = CommunityOrderMapper.toEntity(communityEventsDto);
        coCommunityEvents=communityEventsRepository.save(coCommunityEvents);

        // ADD a record in group order invitation table to continue flow as group order
        GroupOrderInvitationDto groupOrderInvitationDto = new GroupOrderInvitationDto();

        groupOrderInvitationDto.setCreatedBy(communityEventsDto.getCreatedBy());
        groupOrderInvitationDto.setStatus(COConstants.GROUP_ORDER_INVITATION_CREATED);
        groupOrderInvitationDto.setOrderType(COConstants.COMMUNITY_ORDER_ORDER_TYPE);

        LocalDateTime bookingStartDate = communityEventsDto.getBookingStartDate();
        LocalDateTime bookingEndDate   = communityEventsDto.getBookingEndDate();

        //Convert the duration between booking start date and end date as minutes
        long minutes = ChronoUnit.MINUTES.between(bookingStartDate, bookingEndDate);
        Integer durationInMIns = Integer.valueOf(Math.toIntExact(minutes));
        groupOrderInvitationDto.setOrderCloseDurationInMinutes(durationInMIns);

        groupOrderInvitationDto.setCommunityId(coCommunityEvents.getCommunityId());
        groupOrderInvitationDto.setCommunityEventId(coCommunityEvents.getCommunityEventsId());

        groupOrderInvitationDto.setMaxMembers(communityEventsDto.getMaxMembers());
        groupOrderInvitationDto.setOutletId(communityEventsDto.getOutletId());
        groupOrderInvitationDto.setPaymentResponsibility(COConstants.COMMUNITY_ORDER_PAYMENT_TYPE);

        ResponseEntity<?>  groupOrderResponse = groupOrderService.createGroupOrderInvitation(groupOrderInvitationDto);
        GroupOrderInvitationDto savedGroupOrder = (GroupOrderInvitationDto) groupOrderResponse.getBody();
        Integer groupOrderInvitationId = 0;
        if(savedGroupOrder != null){
            groupOrderInvitationId = savedGroupOrder.getGroupOrdersInvitationId();
            String redisKey = "event:activate:invitation:" + groupOrderInvitationId;
            long delayInSeconds = Duration.between(communityEventsDto.getEventStartDate(),
                    communityEventsDto.getBookingStartDate()).getSeconds();
            try{

                redisTemplate.opsForValue().set(redisKey, "ACTIVE", delayInSeconds, TimeUnit.SECONDS);
                log.info("data is stored in redis key : {}and value is ACTIVE and expires in seconds{}", redisKey, delayInSeconds);
            }catch (Exception e){
                e.printStackTrace();
                log.warn("Redis error, please try again after some time ");
                throw new RuntimeException("Redis error: Unable to activate invitation. Rolling back changes.", e);
            }


        }

        log.info("Community events :{} saved successfully ",communityEventsDto.getEventTitle() );

        return ResponseEntity.status(201).body(new CoResponseDto("201",
                "Community events: "+communityEventsDto.getEventTitle() +" saved successfully " ));
    }

     public static ResponseEntity<CoResponseDto> validateEventDates(
            LocalDateTime eventStartDate,
            LocalDateTime eventEndDate,
            LocalDateTime bookingStartDate,
            LocalDateTime bookingEndDate) {

        // 1. Event Start Date must be before Event End Date
        if (eventStartDate.isAfter(eventEndDate) || eventStartDate.isEqual(eventEndDate)) {
            log.warn("Event start date must be strictly before the event end date.");
            return ResponseEntity.status(500).body(new CoResponseDto("500",
                    "Event start date must be strictly before the event end date."));
        }

        // 2. Booking Start Date must be before Booking End Date
        if (bookingStartDate.isAfter(bookingEndDate) || bookingStartDate.isEqual(bookingEndDate)) {
            log.warn("Booking start date must be strictly before the booking end date.");
            return ResponseEntity.status(500).body(new CoResponseDto("500",
                    "Booking start date must be strictly before the booking end date."));
        }

        // 3. Booking Start Date must exist before Event End Date
        if (bookingStartDate.isAfter(eventEndDate)) {
            log.warn("Booking window cannot start after the event has already ended.");
            return ResponseEntity.status(500).body(new CoResponseDto("500",
                    "Booking window cannot start after the event has already ended."));
        }

        // 4. Booking End Date must exist before Event End Date
        if (bookingEndDate.isAfter(eventEndDate)) {
            log.warn("Booking window must close before the event ends");
            return ResponseEntity.status(500).body(new CoResponseDto("500",
                    "Booking window must close before the event ends."));
        }
        return null;
    }

    @Override
    public ResponseEntity<CoResponseDto> AddOrDropMembersFromCommunity(CoAddOrDropMembersFromCommunityDto addOrDropMembersFromCommunityDto) {

        ResponseEntity<CoZoneResponseDto> zoneResponseDtoResponseEntity = driverFeignClient
                .findCommunityById(addOrDropMembersFromCommunityDto.getCommunityId());

        CoZoneResponseDto zoneResponseDto = zoneResponseDtoResponseEntity.getBody();
        if(zoneResponseDto.getZoneId() == null){
            log.warn("Community does not exist");
            return  ResponseEntity.status(500).body(new CoResponseDto("500", "Community does not exist "));
        }
        if(addOrDropMembersFromCommunityDto.getType().equals(COConstants.JOIN_COMMUNITY_TYPE)){
            CoCustomerCommunities customerCommunities = CommunityOrderMapper.toCustomerCommunity(addOrDropMembersFromCommunityDto);
            CoCustomerCommunities savedCustomerCommunity =customerCommunityRepository.save(customerCommunities);

            log.info(" Customer with ID : {}, is added to community with ID: {} ",
                    savedCustomerCommunity.getCustomerId(),savedCustomerCommunity.getCommunityId());

            return  ResponseEntity.status(200).body(new CoResponseDto("200", "Customer with ID :"+ savedCustomerCommunity.getCustomerId()+
                    " is added to community : "+zoneResponseDto.getZoneName()));
        }
        if(addOrDropMembersFromCommunityDto.getType().equals(COConstants.DROP_COMMUNITY_TYPE)){

           Optional<CoCustomerCommunities> customerCommunities =  customerCommunityRepository.
                   findByCustomerIdAndCommunityId(addOrDropMembersFromCommunityDto.getCustomerId()
                    ,addOrDropMembersFromCommunityDto.getCommunityId());

           if(customerCommunities.isPresent()){
               customerCommunityRepository.delete(customerCommunities.get());
               log.info(" Customer with ID: {} is deleted from community :{} ",
                       addOrDropMembersFromCommunityDto.getCustomerId(),zoneResponseDto.getZoneName());
               return  ResponseEntity.status(200).body(new CoResponseDto("200", "Customer with ID :"+ addOrDropMembersFromCommunityDto.getCustomerId()+
                       " is dropped from community : "+zoneResponseDto.getZoneName()));
           }else{
               log.warn("Customer with ID: {} is not in the community : {} ",
                       addOrDropMembersFromCommunityDto.getCustomerId(),zoneResponseDto.getZoneName());
               return  ResponseEntity.status(500).body(new CoResponseDto("500", "Customer with ID :"+ addOrDropMembersFromCommunityDto.getCustomerId()+
                       " is not in community : "+zoneResponseDto.getZoneName()));
           }
        }
        return  null;
    }

    @Override
    public ResponseEntity<Integer> findCustomerInCommunity(Double latitude, Double longitude) {

        ResponseEntity<Integer> responseEntity = driverFeignClient.findCustomerInCommunity(latitude,longitude);
        Integer response = responseEntity.getBody();
        return ResponseEntity.status(200).body(response);
    }


}
