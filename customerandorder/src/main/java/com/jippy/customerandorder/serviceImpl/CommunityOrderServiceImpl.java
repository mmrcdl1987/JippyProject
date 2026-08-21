package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCommunity;
import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;
import com.jippy.customerandorder.exception.CommunityZoneException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CommunityOrderService;
import com.jippy.customerandorder.mapper.CommunityOrderMapper;
import com.jippy.customerandorder.projection.CoActiveCommunityGroupOrdersProjection;
import com.jippy.customerandorder.repository.CoCommunityEventsRepository;
import com.jippy.customerandorder.repository.CoCommunityRepository;
import com.jippy.customerandorder.repository.CoCustomerCommunityRepository;

import com.jippy.customerandorder.repository.GroupOrderInvitationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityOrderServiceImpl implements CommunityOrderService {

    private final CoCommunityEventsRepository communityEventsRepository;
    private final FMFeignClient fmFeignClient;
    private final CoCustomerCommunityRepository customerCommunityRepository;
    private final GroupOrderServiceImpl groupOrderService;
    private final CoCommunityRepository communityRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final ImageValidationService imageValidationService;
    private final S3ImageService s3ImageService;
    private final GroupOrderInvitationRepository groupOrderInvitationRepository;

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


        Optional<CoCommunity> community = communityRepository.findByCommunityId(addOrDropMembersFromCommunityDto.getCommunityId());

        if(community.isEmpty()){
            log.warn("Community does not exist");
            return  ResponseEntity.status(500).body(new CoResponseDto("500", "Community does not exist "));
        }
        if(addOrDropMembersFromCommunityDto.getType().equals(COConstants.JOIN_COMMUNITY_TYPE)){
            CoCustomerCommunities customerCommunities = CommunityOrderMapper.toCustomerCommunity(addOrDropMembersFromCommunityDto);
            CoCustomerCommunities savedCustomerCommunity =customerCommunityRepository.save(customerCommunities);

            log.info(" Customer with ID : {}, is added to community with ID: {} ",
                    savedCustomerCommunity.getCustomerId(),savedCustomerCommunity.getCommunityId());

            return  ResponseEntity.status(200).body(new CoResponseDto("200", "Customer with ID :"+ savedCustomerCommunity.getCustomerId()+
                    " is added to community : "+community.get().getCommunityName()));
        }
        if(addOrDropMembersFromCommunityDto.getType().equals(COConstants.DROP_COMMUNITY_TYPE)){

           Optional<CoCustomerCommunities> customerCommunities =  customerCommunityRepository.
                   findByCustomerIdAndCommunityId(addOrDropMembersFromCommunityDto.getCustomerId()
                    ,addOrDropMembersFromCommunityDto.getCommunityId());

           if(customerCommunities.isPresent()){
               customerCommunityRepository.delete(customerCommunities.get());
               log.info(" Customer with ID: {} is deleted from community :{} ",
                       addOrDropMembersFromCommunityDto.getCustomerId(),community.get().getCommunityName());
               return  ResponseEntity.status(200).body(new CoResponseDto("200", "Customer with ID :"+ addOrDropMembersFromCommunityDto.getCustomerId()+
                       " is dropped from community : "+community.get().getCommunityName()));
           }else{
               log.warn("Customer with ID: {} is not in the community : {} ",
                       addOrDropMembersFromCommunityDto.getCustomerId(),community.get().getCommunityName());
               return  ResponseEntity.status(500).body(new CoResponseDto("500", "Customer with ID :"+ addOrDropMembersFromCommunityDto.getCustomerId()+
                       " is not in community : "+community.get().getCommunityName()));
           }
        }
        return  null;
    }

    @Override
    public ResponseEntity<?> findCustomerInCommunity(Double latitude, Double longitude) {

        Optional<CoCommunity> optionalCommunity =communityRepository.findCustomerInCommunity(latitude,longitude);
        if(optionalCommunity.isPresent()){
            CoCommunity community = optionalCommunity.get();

            String areaName =fmFeignClient.findAreaById(community.getCommunityAreaId());

            CoCommunityResponseDto communityResponseDto = CommunityOrderMapper.toCommunityResponseDto(community,areaName);

            return ResponseEntity.status(200).body(communityResponseDto);
        }

        return ResponseEntity.status(404).body(new CoResponseDto("404", "No community found for the given coordinates."));
    }

    @Override
    public String createCommunity(CommunityDto communityDto, MultipartFile communityImage) {

        try{
            Optional<CoCommunity> existingCommunity = communityRepository.findByCommunityName(communityDto.getCommunityName());
            Polygon polygon = convertToPolygon(communityDto.getBoundary());

            imageValidationService.validateImage(communityImage);

            if (existingCommunity.isPresent()) {
                if (communityRepository.existsBySpatialBoundary(polygon)) {
                    throw new CommunityZoneException("A boundary with this exact shape already exists!");
                } else {
                    log.info("Updating existing community with id: {}", existingCommunity.get().getCommunityId());
                    CoCommunity communityToUpdate = existingCommunity.get();
                    CoCommunity community = CommunityOrderMapper.mapToCommunityEntity(communityToUpdate,communityDto, polygon);
                    String s3BucketFilePath = s3ImageService.uploadFile(communityImage,"communities"+communityToUpdate.getCommunityId());
                    communityRepository.save(communityToUpdate);
                    return "Community:" + communityToUpdate.getCommunityName() + " updated successfully!";
                }
            }
            CoCommunity community = new CoCommunity();
            community = CommunityOrderMapper.mapToCommunityEntity(community, communityDto, polygon);
            CoCommunity savedCommunity = communityRepository.save(community);
            log.info("New Community is created with id: {}", savedCommunity.getCommunityId());

            String s3BucketFilePath = s3ImageService.uploadFile(communityImage,"communities"+savedCommunity.getCommunityId());
            savedCommunity.setCommunityImageUrl(s3BucketFilePath);
            communityRepository.save(savedCommunity);

            log.info("File uploaded to S3 successfully. Bucket path: {}", s3BucketFilePath);


            return "Community:" + savedCommunity.getCommunityName() + " created successfully!";

        } catch (Exception e) {
            log.error("An unexpected error occurred while creating/updating the community: {}", e.getMessage());
            throw new RuntimeException("An unexpected error occurred while creating/updating the community.", e);
        }


    }

    public Polygon convertToPolygon(List<List<CommunityDto.CoordinateDTO>> boundary) {
        if (boundary == null || boundary.isEmpty()) {
            throw new IllegalArgumentException("Boundary coordinates cannot be empty");
        }

        // 1. Convert the exterior ring (index 0)
        List<CommunityDto.CoordinateDTO> exteriorCoords = boundary.get(0);
        Coordinate[] exteriorCoordinates = mapAndCloseCoordinates(exteriorCoords);
        LinearRing exteriorRing = geometryFactory.createLinearRing(exteriorCoordinates);

        // 2. Convert interior rings (holes), if any exist (index 1 to N)
        LinearRing[] holes = null;
        if (boundary.size() > 1) {
            holes = new LinearRing[boundary.size() - 1];
            for (int i = 1; i < boundary.size(); i++) {
                Coordinate[] holeCoords = mapAndCloseCoordinates(boundary.get(i));
                holes[i - 1] = geometryFactory.createLinearRing(holeCoords);
            }
        }

        // 3. Construct single Polygon
        Polygon polygon = geometryFactory.createPolygon(exteriorRing, holes);
        polygon.setSRID(4326);

        return polygon;
    }

    private Coordinate[] mapAndCloseCoordinates(List<CommunityDto.CoordinateDTO> dtos) {
        List<Coordinate> coords = dtos.stream()
                .map(c -> new Coordinate(c.getLongitude(), c.getLatitude()))
                .collect(Collectors.toList());

        // JTS requires at least 4 coordinates for a valid ring (3 vertices + 1 closing point)
        if (coords.size() < 3) {
            throw new IllegalArgumentException("A polygon ring must have at least 3 distinct coordinates");
        }

        // Ensure the ring is explicitly closed
        if (!coords.get(0).equals2D(coords.get(coords.size() - 1))) {
            coords.add(new Coordinate(coords.get(0).x, coords.get(0).y));
        }

        return coords.toArray(new Coordinate[0]);
    }


    @Override
    public ResponseEntity<?> getActiveCommunityGroupOrders(Integer communityId) {

        List<CoActiveCommunityGroupOrdersProjection> activeCommunityGroupOrders = groupOrderInvitationRepository.findActiveCommunityGroupOrders(communityId);

        if (activeCommunityGroupOrders.isEmpty()) {
            log.info("No active community group orders found.");
            return ResponseEntity.status(404).body(new CoResponseDto("404", "No active community group orders found."));
        }

        List<CoActiveGroupOrdersResponseDto> groupOrdersResponseDtos = new ArrayList<>();

        for (CoActiveCommunityGroupOrdersProjection groupOrder : activeCommunityGroupOrders) {

            CoActiveGroupOrdersResponseDto groupOrdersResponseDto = CommunityOrderMapper.toCommunityOrdersResponseDto(groupOrder);
            groupOrdersResponseDtos.add(groupOrdersResponseDto);
        }

        log.info("Found {} active community group orders.", groupOrdersResponseDtos.size());
        return ResponseEntity.status(200).body(groupOrdersResponseDtos);
    }

}
