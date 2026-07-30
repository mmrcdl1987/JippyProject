package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.exception.CoResourceNotFoundException;
import com.jippy.customerandorder.feignClients.DriverFeignClient;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.GroupOrderService;
import com.jippy.customerandorder.mapper.GroupOrderMapper;
import com.jippy.customerandorder.projection.CustomerDeliveryAddressProjection;
import com.jippy.customerandorder.projection.GroupOrderCartItemsProjection;
import com.jippy.customerandorder.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupOrderServiceImpl implements GroupOrderService {


    private final GroupOrderInvitationRepository groupOrderInvitationRepository;
    private final CoCustomerRepository customerRepository;
    private final CoGroupOrderMemberRepository groupOrderMemberRepository;
    private final GroupCartItemsRepository groupCartItemsRepository;
    private final CoOrderSettingsRepository orderSettingsRepository;
    private final FMFeignClient fmFeignClient;
    private final StringRedisTemplate redisTemplate;
    private final DriverFeignClient driverFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GroupOrderPriceBreakupRepository priceBreakupRepository;
    private final GroupOrderPaymentRepository paymentRepository;
    private final COOrderService orderService;
    private  final CoOrderRepository orderRepository;
    private final CoCustomerCommunityRepository customerCommunityRepository;
    private final CoCustomerDeliveryAddressRepository customerDeliveryAddressRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC = "group-order-events";

    @Override
    public ResponseEntity<?> createGroupOrderInvitation(GroupOrderInvitationDto groupCreationDto) {

        CoCustomer customer = new CoCustomer();

        log.info("{} flow started ",groupCreationDto.getOrderType());

        ResponseEntity<?> validationResponse = validateOrderType(groupCreationDto,customer);

        // If validation returned an error status, stop immediately and return it to Postman
        if (validationResponse != null && validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        GroupOrderInvitation groupOrderInvitation = GroupOrderMapper.toGroupOrderInvitation(groupCreationDto, customer,generateRandomCode());
        GroupOrderInvitation savedGroupOrderInvitation = groupOrderInvitationRepository.save(groupOrderInvitation);

        log.info("Group order invitation created with ID: {} and Invitation Code: {}",
                savedGroupOrderInvitation.getGroupOrdersInvitationId(),
                savedGroupOrderInvitation.getInvitationCode());

        // 2. Create a unique key using the generated database ID
        String redisKey = "group:expiry:" + savedGroupOrderInvitation.getGroupOrdersInvitationId();

        // 3. Save to Redis with expiry duration
        // We don't care about the value ("value" is fine), we only care about the key's lifetime!
        redisTemplate.opsForValue().set(redisKey, "ACTIVE", groupCreationDto.getOrderCloseDurationInMinutes(), TimeUnit.MINUTES);

        log.info("Redis key {} set with :{} expiration for group order invitation ID: {}",
                redisKey, groupCreationDto.getOrderCloseDurationInMinutes(),savedGroupOrderInvitation.getGroupOrdersInvitationId());

        GroupOrderInvitationDto responseDto = GroupOrderMapper.toGroupOrderInvitationResponseDto(savedGroupOrderInvitation);
        return ResponseEntity.ok(responseDto);

    }

    private ResponseEntity<?> validateOrderType(GroupOrderInvitationDto groupCreationDto, CoCustomer customer) {

        if(groupCreationDto.getOrderType().equals(COConstants.COMMUNITY_GROUP_ORDER_ORDER_TYPE)){
            Optional<CoCustomerCommunities> customerCommunities = customerCommunityRepository.
                    findByCustomerIdAndCommunityId(groupCreationDto.getHostCustomerId(),groupCreationDto.getCommunityId());

            if(customerCommunities.isEmpty()){
                log.warn("Community group order is not allowed as customer does not belongs to any community");
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body
                        ("Community group order is not allowed as customer does not belongs to any community " + groupCreationDto.getHostCustomerId());
            }
        }

        if(groupCreationDto.getOrderType().equals(COConstants.GROUP_ORDER_ORDER_TYPE)){

            customer = customerRepository.findById(groupCreationDto.getHostCustomerId())
                    .orElseThrow(() -> {
                        log.error("Customer not found with id: {}", groupCreationDto.getHostCustomerId());
                        return new CoBadRequestException("Customer not found with id: " + groupCreationDto.getHostCustomerId());
                    });

            Optional<GroupOrderInvitation> existingActiveGO = groupOrderInvitationRepository.getActiveGroupOrderByCustomerId(groupCreationDto.getHostCustomerId(), COConstants.GROUP_ORDER_INVITATION_ACTIVE);
            if(existingActiveGO.isPresent()){
                log.error("An active group order is associated with this customer id: {}", groupCreationDto.getHostCustomerId());
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body
                        ("An active group order is associated with this customer id: " + groupCreationDto.getHostCustomerId());
            }

        }
        return null;
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Transactional
    @Override
    public ResponseEntity<CoResponseDto> joinGroupMembers(JoinGroupMembersDto joinGroupMembersDto) {

        // 1. Fetch the active group invitation session using the code from the shared link
        GroupOrderInvitation groupOrderInvitation = groupOrderInvitationRepository.findByGroupOrdersInvitationId(joinGroupMembersDto.getGroupOrdersInvitationId())
                .orElseThrow(() -> {
                    log.error("Group order invitation not found with code: {}", joinGroupMembersDto.getInvitationCode());
                    return new CoResourceNotFoundException("Group order invitation not found with code: "
                            + joinGroupMembersDto.getInvitationCode());
                });

        ResponseEntity<CoResponseDto> validationResponse = validateGroupOrderMembers(joinGroupMembersDto,groupOrderInvitation);

        // If validation returned an error status, stop immediately and return it to Postman
        if (validationResponse != null && validationResponse.getStatusCode().isError()) {
            return validationResponse;
        }

        CoCustomer customer = customerRepository.findById(joinGroupMembersDto.getCustomerId())
                .orElseThrow(() -> {
                    log.error("Customer not found with id: {}", joinGroupMembersDto.getCustomerId());
                    return new CoBadRequestException("Customer not found with id: "
                            + joinGroupMembersDto.getCustomerId());
                });


        GroupOrderMembers groupOrderMembersEntity = GroupOrderMapper.toGroupOrderMembersEntity
                (joinGroupMembersDto, groupOrderInvitation, customer);

        groupOrderMemberRepository.save(groupOrderMembersEntity);

        log.info("Customer with ID {} successfully joined group order with code {}. GroupOrderMembers ID: {}",
                joinGroupMembersDto.getCustomerId(), joinGroupMembersDto.getInvitationCode(), groupOrderMembersEntity.getGroupOrderMembersId());

        // Note: After saving, you can trigger a Kafka event or Firebase Push Notification
        // to tell the host's app UI to update instantly with "+1 member joined"!

        GroupOrderEventDto eventPayload = new GroupOrderEventDto();
        eventPayload.setEventType("MEMBER_JOINED");
        eventPayload.setGroupOrdersInvitationId(groupOrderInvitation.getGroupOrdersInvitationId());
        eventPayload.setCustomerId(joinGroupMembersDto.getCustomerId());
        eventPayload.setCustomerName(customer.getFirstName());
        eventPayload.setDeliveryAddressId(joinGroupMembersDto.getDeliveryAddressId());

        // 3. Publish to Kafka Topic asynchronous broker pipeline
        // Using invitationId as the Kafka Partitioning Key ensures sequence ordering per room session
        this.kafkaTemplate.send(TOPIC, String.valueOf(groupOrderInvitation.getGroupOrdersInvitationId()), eventPayload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent member joined event to Kafka for Group: {}",
                                groupOrderInvitation.getGroupOrdersInvitationId());
                    } else {
                        log.error("Failed to stream event notification to Kafka", ex);
                    }
                });

        return ResponseEntity.ok(new CoResponseDto("200", "Customer with ID: "+joinGroupMembersDto.getCustomerId()
                + " successfully joined group order with id "+ joinGroupMembersDto.getGroupOrdersInvitationId()));

    }

    private ResponseEntity<CoResponseDto> validateGroupOrderMembers(JoinGroupMembersDto joinGroupMembersDto, GroupOrderInvitation groupOrderInvitation) {

        // 2. Validate Time Lock: Check if the 30 minutes expired
        LocalDateTime currentTime = groupOrderInvitation.getCreatedAt();
        LocalDateTime expirationTime = currentTime.plusMinutes(30);

        if (LocalDateTime.now().isAfter(expirationTime)) {
            groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_INVITATION_EXPIRED);
            groupOrderInvitationRepository.save(groupOrderInvitation);
            log.warn("Group order invitation with code {} has expired. Current time: {}, Expiration time: {}",
                    joinGroupMembersDto.getInvitationCode(), LocalDateTime.now(), expirationTime);
            throw new IllegalStateException("This group order session has expired.");
        }

        // 3. Validate Status
        if (!"ACTIVE".equals(groupOrderInvitation.getStatus()) && !"LOCKED".equals(groupOrderInvitation.getStatus())) {
            log.warn("Attempt to join group order with code {} which is not active. Current status: {}",
                    joinGroupMembersDto.getInvitationCode(), groupOrderInvitation.getStatus());
            return ResponseEntity.ok(new CoResponseDto("500", "This group :  "
                    +groupOrderInvitation.getInvitationCode()+" is no longer active"));
        }

        // 4. Validate Capacity Limit
        long currentMemberCount = groupOrderMemberRepository.getMaxMembersCount(groupOrderInvitation.getGroupOrdersInvitationId());
        if (currentMemberCount >= groupOrderInvitation.getMaxMembers()) {
            log.warn("Attempt to join full group order with code {}. Current members: {}, Max members: {}",
                    joinGroupMembersDto.getInvitationCode(), currentMemberCount, groupOrderInvitation.getMaxMembers());
            return ResponseEntity.ok(new CoResponseDto("500", "Group is full! Maximum limit reached in the group :  "
                    +groupOrderInvitation.getInvitationCode()));
        }

        // 5. Check if user is already a member to prevent duplicates
        GroupOrderMembers groupOrderMembers = groupOrderMemberRepository.validateGroupMemberAlreadyExists(
                groupOrderInvitation.getGroupOrdersInvitationId(), joinGroupMembersDto.getCustomerId());

        if(groupOrderMembers != null){
            if(groupOrderMembers.getGroupOrderMembersId() != null) {
                log.warn("Customer with ID {} attempted to join group order with code {} but is already a member.",
                        joinGroupMembersDto.getCustomerId(), joinGroupMembersDto.getInvitationCode());
                return ResponseEntity.ok(new CoResponseDto("200", "You have already joined this group order!"));
            }
        }

        if(groupOrderInvitation.getOrderType().equals(COConstants.COMMUNITY_ORDER_ORDER_TYPE)){
            ResponseEntity<CoResponseDto> validationResponse = validateCommunityOrderMember(groupOrderInvitation,joinGroupMembersDto);
            // If validation returned an error status, stop immediately and return it to Postman
            if (validationResponse != null && validationResponse.getStatusCode().isError()) {
                return validationResponse;
            }
        }else if(groupOrderInvitation.getOrderType().equals(COConstants.COMMUNITY_GROUP_ORDER_ORDER_TYPE)){

            CustomerDeliveryAddressProjection deliveryAddressProjection= customerDeliveryAddressRepository
                    .findByDeliveryAddressId(joinGroupMembersDto.getDeliveryAddressId());

            ResponseEntity<Integer> responseEntity =  driverFeignClient.findCustomerInCommunity
                    (deliveryAddressProjection.getLatitude(),deliveryAddressProjection.getLongitude());
            Integer response = responseEntity.getBody();

            if(response == 0){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CoResponseDto("400", "Given delivery address does not belongs to community"));
            }
        }else{
            if(joinGroupMembersDto.getDeliveryAddressId() == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CoResponseDto("400", "Delivery address is required for the first member (Host)."));
            }

        }

        return null;
    }

    private ResponseEntity<CoResponseDto> validateCommunityOrderMember(GroupOrderInvitation groupOrderInvitation, JoinGroupMembersDto joinGroupMembersDto) {

        Optional<CoCommunityEvents> coCommunityEvents = customerCommunityRepository.isCustomerInCommunityForInvitation
                (groupOrderInvitation.getGroupOrdersInvitationId(),joinGroupMembersDto.getCustomerId());
        if(coCommunityEvents.isEmpty()){
            log.error("Customer does not belongs to this community {}", joinGroupMembersDto.getCustomerId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CoResponseDto("500",
                    "Customer does not belongs to this community"));
        }
        //  Check if this is the FIRST member joining this group order invitation
        Optional<GroupOrderMembers> groupOrderMembers1 = groupOrderMemberRepository.getMembersByGroupOrdersInvitationId(
                groupOrderInvitation.getGroupOrdersInvitationId());

        if (groupOrderMembers1.isEmpty()) {
            if (joinGroupMembersDto.getDeliveryAddressId() == null) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CoResponseDto("400", "Delivery address is required for the first member (Host)."));
            }
            // Get first customer added in community and save them as host
            groupOrderInvitation.setHostCustomerId(joinGroupMembersDto.getCustomerId());
            groupOrderInvitationRepository.save(groupOrderInvitation);
        }else{
            joinGroupMembersDto.setDeliveryAddressId(groupOrderMembers1.get().getDeliveryAddressId());
        }

        CustomerDeliveryAddressProjection deliveryAddressProjection= customerDeliveryAddressRepository
                .findByDeliveryAddressId(joinGroupMembersDto.getDeliveryAddressId());
        log.info("========================================="+deliveryAddressProjection.getLatitude()+deliveryAddressProjection.getLongitude()+coCommunityEvents.get().getCommunityId());

        ResponseEntity<Integer> responseEntity =  driverFeignClient.checkCustomerAddressWithCommunity
                (deliveryAddressProjection.getLatitude(),deliveryAddressProjection.getLongitude(),coCommunityEvents.get().getCommunityId());

        Integer respone = responseEntity.getBody();
        if(respone == 0){
            log.error("Customer address is not community address {}", joinGroupMembersDto.getDeliveryAddressId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CoResponseDto("404",
                    "Customer address is not community address "));
        }
        return  null;
    }

    @Transactional
    @Override
    public ResponseEntity<CoResponseDto> addItemsToGroupCart(CoGroupCartItemsDto groupCartItemsDto) {

        //  Fetch the active group invitation session using the code from the shared link
        GroupOrderInvitation groupOrderInvitation = groupOrderInvitationRepository.findById(groupCartItemsDto.getGroupOrderInvitationId())
                .orElseThrow(() -> {
                    log.error("Group order invitation not found with ID: {}", groupCartItemsDto.getGroupOrderInvitationId());
                    return new CoResourceNotFoundException("Group order invitation not found with ID: "
                            + groupCartItemsDto.getGroupOrderInvitationId());
                });

        // Check whether the group session status is active or not
        if (!"ACTIVE".equals(groupOrderInvitation.getStatus())) {
            log.warn("Attempt to add items to group cart for invitation ID {} which is not active. Current status: {}",
                    groupCartItemsDto.getGroupOrderInvitationId(), groupOrderInvitation.getStatus());
            throw new IllegalStateException("This group order is locked and no longer accepting items.");
        }

        //  Check whether the customer exists or not
        CoCustomer customer = customerRepository.findById(groupCartItemsDto.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Customer with ID: {} does not belong to this group ", groupCartItemsDto.getCustomerId());
                    return new CoBadRequestException("Customer not found with id: "
                            + groupCartItemsDto.getCustomerId());
                });
        //  Business Validation: Verify this customer actually joined this specific group session first
        boolean isMember = groupOrderMemberRepository.existsByGroupOrdersInvitationAndCustomer(groupOrderInvitation, customer);
        if (!isMember) {
            throw new IllegalStateException("Access Denied: Customer must join the group invitation before adding items to the cart.");
        }

        // 6. Everything is valid! Proceed to save or update the item inside group_cart_items
        Optional<GroupCartItems> existingCartItem = groupCartItemsRepository
                .findByGroupOrdersAndCustomerAndProductId(groupOrderInvitation.getGroupOrdersInvitationId(),
                        customer.getCustomerId(),
                        groupCartItemsDto.getProductId());

        if (existingCartItem.isPresent()) {
            // If the item already exists in their basket, update the quantity
            GroupCartItems cartItem = existingCartItem.get();

            if(groupCartItemsDto.getQuantity() == 0){
                groupCartItemsRepository.delete(cartItem);
                sendWebSocketEvent(cartItem,groupOrderInvitation,COConstants.ACTION_ITEM_REMOVED);

                log.info("Customer with ID {} successfully removed Product ID {} from group cart for invitation ID {}.",
                        groupCartItemsDto.getCustomerId(), groupCartItemsDto.getProductId(), groupCartItemsDto.getGroupOrderInvitationId());

                return ResponseEntity.ok(new CoResponseDto("200", "Item successfully removed from the group cart!"));
            }

            cartItem.setQuantity(groupCartItemsDto.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
            groupCartItemsRepository.save(cartItem);

            sendWebSocketEvent(cartItem,groupOrderInvitation,COConstants.ACTION_QUANTITY_UPDATED);

            log.info("Customer with ID {} successfully updated quantity with Product ID {} to group cart for invitation ID {}.",
                    groupCartItemsDto.getCustomerId(), groupCartItemsDto.getProductId(), groupCartItemsDto.getGroupOrderInvitationId());

            return ResponseEntity.ok(new CoResponseDto("200", "Item qunatity successfully updated to the group cart!"));

        } else {
            // If it's a completely new dish being added
            GroupCartItems newCartItem = new GroupCartItems();
            newCartItem.setGroupOrders(groupOrderInvitation);
            newCartItem.setCustomer(customer);
            newCartItem.setProductId(groupCartItemsDto.getProductId());
            newCartItem.setQuantity(groupCartItemsDto.getQuantity());

            // Set prices (Usually pulled via a Product/Menu Service)
            newCartItem.setOnlineUnitPrice(groupCartItemsDto.getOnlineUnitPrice());
           // newCartItem.setMerchantUnitPrice(groupCartItemsDto.getMerchantUnitPrice());

            newCartItem.setCreatedAt(LocalDateTime.now());
            newCartItem.setUpdatedAt(LocalDateTime.now());

            groupCartItemsRepository.save(newCartItem);

            sendWebSocketEvent(newCartItem,groupOrderInvitation,COConstants.ACTION_ITEM_ADDED);

            log.info("Customer with ID {} successfully added/updated item with Product ID {} to group cart for invitation ID {}.",
                    groupCartItemsDto.getCustomerId(), groupCartItemsDto.getProductId(), groupCartItemsDto.getGroupOrderInvitationId());

            return ResponseEntity.ok(new CoResponseDto("200", "Item successfully added to the group cart!"));
        }

    }

    private void sendWebSocketEvent(GroupCartItems cartItem, GroupOrderInvitation groupOrderInvitation, String action) {

        CoFmProductDto productDto = fmFeignClient.getSettlementProductById(cartItem.getProductId());

        // 2. Build update event payload
        CartUpdateEventDto event = new CartUpdateEventDto(
                action,
                groupOrderInvitation.getGroupOrdersInvitationId(),
                cartItem.getCustomer().getCustomerId(),
                cartItem.getCustomer().getFirstName() + " " +cartItem.getCustomer().getLastName(),
                productDto.getProductName(),
                cartItem.getQuantity(),
                cartItem.getOnlineUnitPrice()
        );

        // 3. Broadcast to all subscribers of this specific group invitation
        String destination = "/topic/group-order/" + groupOrderInvitation.getGroupOrdersInvitationId();
        log.info("Broadcasting cart update to channel: {}", destination);

        messagingTemplate.convertAndSend(destination, event);
    }

    @Override
    public ResponseEntity<GroupOrderCheckoutDto> groupOrderCheckOut(Integer groupOrdersInvitationId, Integer hostCustomerId,
            Double couponDiscount, Double deliveryTip)
    {
        log.info("Initiating checkout for group order invitation ID: {} by host customer ID: {}", groupOrdersInvitationId, hostCustomerId);

        //  Fetch the active group invitation session using the code from the shared link
        GroupOrderInvitation groupOrderInvitation = groupOrderInvitationRepository.findById(groupOrdersInvitationId)
                .orElseThrow(() -> {
                    log.error("Group order invitation not found with ID: {}", groupOrdersInvitationId);
                    return new CoResourceNotFoundException("Group order invitation not found with ID: "
                            + groupOrdersInvitationId);
                });

        // Check whether the group session status is active or not
        if (!"ACTIVE".equals(groupOrderInvitation.getStatus())) {
            log.warn("Attempt to checkout group order for invitation ID {} which is not active. Current status: {}",
                    groupOrdersInvitationId, groupOrderInvitation.getStatus());
            throw new IllegalStateException("This group order is locked and cannot be checked out.");
        }
        groupOrderInvitation.setStatus(COConstants.GROUP_ORDER_STATUS_LOCKED);
        groupOrderInvitationRepository.save(groupOrderInvitation);

        //get platform fee,surge fee etc.., from OrderSettings

        CoOrderSettings orderSettings = orderSettingsRepository.findAll().stream().findFirst().orElseThrow(() -> {
            log.error("ORDER_SETTINGS_NOT_FOUND");
            return new CoBadRequestException(COConstants.MSG_ORDER_SETTINGS_NOT_FOUND);
        });

        GroupOrderCheckoutDto groupOrderCheckoutDto = new GroupOrderCheckoutDto();
        groupOrderCheckoutDto.setPlatformFee(orderSettings.getPlatformFee());
        groupOrderCheckoutDto.setPackagingFee(orderSettings.getPackagingFee());
        groupOrderCheckoutDto.setSurgeFee(orderSettings.getSurgeFee());
        groupOrderCheckoutDto.setGroupOrdersInvitationId(groupOrdersInvitationId);

        List<GroupOrderCartItemsProjection> groupCartItems = groupCartItemsRepository.findBygroupOrdersInvitationId(groupOrdersInvitationId);
        System.out.println("===============cart items===============" + groupCartItems.size());

//  Use a nested map structure -> Map<AddressId, Map<CustomerId, GroupOrderCheckoutItemsDto>>
        Map<Integer, Map<Integer, GroupOrderCheckoutItemsDto>> addressCustomerMap = new LinkedHashMap<>();

// Also keep track of address containers to aggregate total costs per address
        Map<Integer, GroupOrderDeliveryCheckOutItemsDto> addressContainerMap = new LinkedHashMap<>();

        for (GroupOrderCartItemsProjection cartItem : groupCartItems) {
            Integer currentAddressId = cartItem.getDeliveryAddressId();
            Integer currentCustomerId = cartItem.getCustomerId();

            // 1. Initialize Address container if it doesn't exist
            if (!addressContainerMap.containsKey(currentAddressId)) {
                GroupOrderDeliveryCheckOutItemsDto deliveryCheckOutItemsDto = new GroupOrderDeliveryCheckOutItemsDto();
                deliveryCheckOutItemsDto.setDeliveryAddressId(currentAddressId);
                deliveryCheckOutItemsDto.setGroupOrderCheckoutItemsDtoList(new ArrayList<>());
                deliveryCheckOutItemsDto.setItemsTotal(BigDecimal.ZERO);

                addressContainerMap.put(currentAddressId, deliveryCheckOutItemsDto);
                addressCustomerMap.put(currentAddressId, new LinkedHashMap<>());
            }
            GroupOrderDeliveryCheckOutItemsDto activeAddressGroup = addressContainerMap.get(currentAddressId);
            Map<Integer, GroupOrderCheckoutItemsDto> customerMapForAddress = addressCustomerMap.get(currentAddressId);

            // 2. Initialize Customer wrapper within this specific address if it doesn't exist
            if (!customerMapForAddress.containsKey(currentCustomerId)) {
                GroupOrderCheckoutItemsDto customerDto = new GroupOrderCheckoutItemsDto();
                customerDto.setCustomerId(currentCustomerId);
                customerDto.setAmountToPay(BigDecimal.ZERO);
                customerDto.setProductsList(new ArrayList<>());

                CoCustomer customer = customerRepository.findById(currentCustomerId).orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", currentCustomerId);
                    return new CoResourceNotFoundException("Customer not found with ID: " + currentCustomerId);
                });
                customerDto.setCustomerName(customer.getFirstName() + " " + customer.getLastName());

                customerMapForAddress.put(currentCustomerId, customerDto);

                activeAddressGroup.getGroupOrderCheckoutItemsDtoList().add(customerDto);


                GroupOrderMembers groupOrderMembers = groupOrderMemberRepository.findByCustomerAndGroupOrdersInvitation(customer,groupOrderInvitation);
                groupOrderMembers.setOrderPlaced(COConstants.GROUP_ORDER_PLACED_TRUE);
                groupOrderMembers.setUpdatedAt(LocalDateTime.now());
                groupOrderMemberRepository.save(groupOrderMembers);
            }

            GroupOrderCheckoutItemsDto activeCustomerGroup = customerMapForAddress.get(currentCustomerId);

            // 3. Build the individual Product DTO
            ProductItemDto productDto = new ProductItemDto();
            productDto.setProductId(cartItem.getProductId());
            productDto.setQuantity(cartItem.getQuantity());
            productDto.setOnlinePrice(cartItem.getOnlineUnitPrice());

            FmProductDetailResponseDto fmProductDetailResponseDto = fmFeignClient.getProductById(cartItem.getProductId());
            productDto.setProductName(fmProductDetailResponseDto.getProductName());

            // Add product to customer's list
            activeCustomerGroup.getProductsList().add(productDto);

            // 4. Calculations (Now using the correct, accessible reference)
            BigDecimal itemCost = cartItem.getOnlineUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            // Accumulate individual customer subtotal sequentially using activeCustomerGroup
            BigDecimal updatedCustomerTotal = activeCustomerGroup.getAmountToPay().add(itemCost);
            activeCustomerGroup.setAmountToPay(updatedCustomerTotal);

            // Accumulate overall address subtotal
            activeAddressGroup.setItemsTotal(activeAddressGroup.getItemsTotal().add(itemCost));
        }

// 3. Post-Process calculations for each unique Address Group
        List<GroupOrderDeliveryCheckOutItemsDto> finalAddressList = new ArrayList<>(addressContainerMap.values());
        BigDecimal totalAmountNet = BigDecimal.ZERO;

        for (GroupOrderDeliveryCheckOutItemsDto addressGroup : finalAddressList) {

            // Call Feign client with the contextual subtotal for this address
            DeliveryChargeCalculationRequestDto deliveryChargeCalculationRequestDto = new DeliveryChargeCalculationRequestDto();
            deliveryChargeCalculationRequestDto.setOutletId(groupOrderInvitation.getOutletId());
            deliveryChargeCalculationRequestDto.setCustomerAddressId(addressGroup.getDeliveryAddressId());
            deliveryChargeCalculationRequestDto.setOrderAmount(addressGroup.getItemsTotal());

            DeliveryChargeCalculationResponseDto deliveryChargeResponse =
                    driverFeignClient.calculateDeliveryCharge(deliveryChargeCalculationRequestDto);

            // Apply delivery specifics
            addressGroup.setDeliveryCharge(deliveryChargeResponse.getDeliveryCharge());
            addressGroup.setDeliveryDistanceKm(deliveryChargeResponse.getDeliveryDistanceKm());
            addressGroup.setTaxAmount(deliveryChargeResponse.getTaxAmount());
            addressGroup.setTotalDeliveryCharge(deliveryChargeResponse.getTotalDeliveryCharge());

            // Calculate Food Tax for this section's items
            BigDecimal foodTax = calculatePercentage(addressGroup.getItemsTotal(), orderSettings.getFoodTotalAmountTax());
            addressGroup.setFoodTax(foodTax);

            // Aggregate this address bundle's financial share to your master summary total
            totalAmountNet = totalAmountNet.add(addressGroup.getItemsTotal())
                    .add(addressGroup.getTotalDeliveryCharge())
                    .add(foodTax);
        }

// 4. Incorporate Global Shared Fixed Fees once at the top level
        totalAmountNet = totalAmountNet.add(orderSettings.getPlatformFee())
                .add(orderSettings.getSurgeFee())
                .add(orderSettings.getPackagingFee())
                .add(deliveryTip != null ? BigDecimal.valueOf(deliveryTip) : BigDecimal.ZERO)
                .subtract(couponDiscount != null ? BigDecimal.valueOf(couponDiscount) : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        groupOrderCheckoutDto.setTotalNetAmount(totalAmountNet);
        groupOrderCheckoutDto.setDeliveryCheckOutItemsDtoList(finalAddressList);

        return ResponseEntity.ok(groupOrderCheckoutDto);
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {

        return amount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }


    @Override
    public ResponseEntity<GroupOrderPaymentDetailsResponseDto> groupPaymentDetails(GroupPaymentDetailsDto groupPaymentDetailsDto) {

        Integer groupOrdersId = groupPaymentDetailsDto.getGroupOrderInvitationId();
        Integer hostId = groupPaymentDetailsDto.getHostCustomerId();
        LocalDateTime now = LocalDateTime.now();

        if (groupPaymentDetailsDto.getGroupOrderDeliveryChargeDetailsDtoList() == null) {
            return ResponseEntity.ok(new GroupOrderPaymentDetailsResponseDto());
        }

        //  Fetch the active group invitation session using the code from the shared link
        GroupOrderInvitation groupOrderInvitation = groupOrderInvitationRepository.findById(groupOrdersId)
                .orElseThrow(() -> {
                    log.error("Group order invitation not found with ID: {}", groupOrdersId);
                    return new CoResourceNotFoundException("Group order invitation not found with ID: "
                            + groupOrdersId);
                });

        // 1. Calculate total unique customers across ALL addresses to split global shared fees fairly
        int totalCustomers = 0;
        for (GroupOrderDeliveryChargeDetailsDto deliveryGroup : groupPaymentDetailsDto.getGroupOrderDeliveryChargeDetailsDtoList()) {
            if (deliveryGroup.getCustomerPaymentDetailsDtoList() != null) {
                totalCustomers += deliveryGroup.getCustomerPaymentDetailsDtoList().size();
                log.info("Total customers in GroupID : {} are : {}",groupOrderInvitation.getGroupOrdersInvitationId(),
                        totalCustomers);
            }
        }

        if (totalCustomers == 0) {
            throw new IllegalArgumentException("Cannot process checkout with zero group customers.");
        }

        // 2. Compute individual shares of global fixed costs
        BigDecimal platformFee = groupPaymentDetailsDto.getPlatformFee() != null ?
                groupPaymentDetailsDto.getPlatformFee() : BigDecimal.ZERO;
        BigDecimal surgeFee = groupPaymentDetailsDto.getSurgeFee() != null ?
                groupPaymentDetailsDto.getSurgeFee() : BigDecimal.ZERO;
        BigDecimal packagingFee = groupPaymentDetailsDto.getPackagingFee() != null ?
                groupPaymentDetailsDto.getPackagingFee() : BigDecimal.ZERO;

        BigDecimal individualSurgeShare = surgeFee.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);
        BigDecimal individualPackagingShare = packagingFee.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);
        BigDecimal individualPlatformShare = platformFee.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);

        log.info("Individual Surge Fee Share :{} ",individualSurgeShare);
        log.info("Individual Packaging Fee Share :{} ",individualPackagingShare);
        log.info("Individual Platform Fee Share :{} ",individualPlatformShare);

        List<GroupOrderPriceBreakup> groupOrderPriceBreakupList = new ArrayList<>();
        List<GroupOrderPayment> groupOrderPaymentList = new ArrayList<>();

        // 3. Iterate over address clusters to compute delivery and tax allocations
        for (GroupOrderDeliveryChargeDetailsDto deliveryGroup : groupPaymentDetailsDto.getGroupOrderDeliveryChargeDetailsDtoList()) {
            List<GroupOrderCustomerPaymentDetailsDto> addressCustomers = deliveryGroup.getCustomerPaymentDetailsDtoList();
            if (addressCustomers == null || addressCustomers.isEmpty()) {
                continue;
            }

            int customerCountAtAddress = addressCustomers.size();

            BigDecimal totalDeliveryCharge = deliveryGroup.getTotalDeliveryCharge() != null
                    ? deliveryGroup.getTotalDeliveryCharge() : BigDecimal.ZERO;
            BigDecimal totalFoodTax = deliveryGroup.getFoodTax() != null ?
                    deliveryGroup.getFoodTax() : BigDecimal.ZERO;

            // Split metrics for this specific location block
            BigDecimal individualDeliveryShare = totalDeliveryCharge.divide(BigDecimal.valueOf(customerCountAtAddress), 2, RoundingMode.HALF_UP);
            BigDecimal individualTaxShare = totalFoodTax.divide(BigDecimal.valueOf(customerCountAtAddress), 2, RoundingMode.HALF_UP);

            log.info("Individual Delivery Fee Share :{} ",individualDeliveryShare);
            log.info("Individual Tax Fee Share :{} ",individualTaxShare);

            for (GroupOrderCustomerPaymentDetailsDto customerDto : addressCustomers) {
                BigDecimal baseOrderAmount = customerDto.getAmountToPay() != null
                        ? customerDto.getAmountToPay() : BigDecimal.ZERO;

                // Sum up everything for this user to get their total bill invoice amount
                BigDecimal finalCustomerTotalAmount = baseOrderAmount
                        .add(individualDeliveryShare)
                        .add(individualSurgeShare)
                        .add(individualPackagingShare)
                        .add(individualTaxShare)
                        .add(individualPlatformShare) // optional, add if platform fee is included in total net billing
                        .setScale(2, RoundingMode.HALF_UP);

                // --- BUILD BREAKDOWN RECORD ---
                GroupOrderPriceBreakup groupOrderPriceBreakup = new GroupOrderPriceBreakup();
                groupOrderPriceBreakup.setGroupOrderInvitation(groupOrderInvitation);
                groupOrderPriceBreakup.setCustomerId(customerDto.getCustomerId());
                groupOrderPriceBreakup.setOrderAmount(baseOrderAmount);
                groupOrderPriceBreakup.setDeliverCharges(individualDeliveryShare);
                groupOrderPriceBreakup.setSurgeFee(individualSurgeShare);
                groupOrderPriceBreakup.setPackagingFee(individualPackagingShare);
                groupOrderPriceBreakup.setGst(individualTaxShare);
                groupOrderPriceBreakup.setOrderTotalAmount(finalCustomerTotalAmount);
                groupOrderPriceBreakup.setCreatedAt(now);
                groupOrderPriceBreakup.setCreatedBy(hostId);

                Optional<GroupOrderPriceBreakup> existingGroupOrderPriceBreakup =
                        priceBreakupRepository.findByGroupOrderInvitationIdAndCustomerId(groupOrderInvitation.getGroupOrdersInvitationId(),
                                customerDto.getCustomerId());
                if(!existingGroupOrderPriceBreakup.isPresent()){
                    groupOrderPriceBreakupList.add(groupOrderPriceBreakup);
                }

                // --- BUILD PENDING PAYMENT TRANSACTION RECORD ---
                GroupOrderPayment groupOrderPayment = new GroupOrderPayment();
                groupOrderPayment.setGroupOrderInvitation(groupOrderInvitation);
                groupOrderPayment.setCustomerId(customerDto.getCustomerId());
                groupOrderPayment.setAmountToPay(finalCustomerTotalAmount);
                groupOrderPayment.setPaymentStatus("PENDING");

                Optional<GroupOrderPayment> existingGroupOrderPayment = paymentRepository.
                        findByGroupInvitationIdAndCustomerId(groupOrderInvitation.getGroupOrdersInvitationId(),customerDto.getCustomerId());
                if(!existingGroupOrderPayment.isPresent()){
                    groupOrderPaymentList.add(groupOrderPayment);
                }

            }
        }

        // 4. Atomically persist both tables inside the transaction scope
        if (!groupOrderPriceBreakupList.isEmpty()) {
            priceBreakupRepository.saveAll(groupOrderPriceBreakupList);
            paymentRepository.saveAll(groupOrderPaymentList);
            log.info("Successfully persisted {} pricing items and {} ledger entries for Group Order: {}",
                    groupOrderPriceBreakupList.size(), groupOrderPaymentList.size(), groupOrdersId);
        }

        GroupOrderPaymentDetailsResponseDto  groupOrderPaymentDetailsResponseDto = generateResponse(groupOrderInvitation.getGroupOrdersInvitationId(),
                groupPaymentDetailsDto.getTotalNetAmount());
        return ResponseEntity.ok(groupOrderPaymentDetailsResponseDto);
    }

    private GroupOrderPaymentDetailsResponseDto generateResponse(Integer groupOrdersInvitationId, BigDecimal totalNetAmount) {


        GroupOrderPaymentDetailsResponseDto groupOrderPaymentDetailsResponseDto = new GroupOrderPaymentDetailsResponseDto();
        groupOrderPaymentDetailsResponseDto.setTotalNetAmount(totalNetAmount);

        List<GroupOrderCustomerPaymentsResponseDto> gcCustomerPaymentResponseDtoList = new ArrayList<>();

        List<GroupOrderPayment> groupOrderPaymentList  = paymentRepository.findAllByGroupInvitationId(groupOrdersInvitationId);

        log.info("Customers list size for payment in group : {} is : {}",groupOrdersInvitationId,groupOrderPaymentList.size());

        for(GroupOrderPayment groupOrderPayment: groupOrderPaymentList){
            GroupOrderCustomerPaymentsResponseDto groupOrderCustomerPaymentsResponseDto = new GroupOrderCustomerPaymentsResponseDto();

            groupOrderCustomerPaymentsResponseDto.setGroupOrderPaymentsId(groupOrderPayment.getGroupOrderPaymentId());
            groupOrderCustomerPaymentsResponseDto.setCustomerId(groupOrderPayment.getCustomerId());
            groupOrderCustomerPaymentsResponseDto.setAmountToPay(groupOrderPayment.getAmountToPay());
            groupOrderCustomerPaymentsResponseDto.setPaymentStatus(groupOrderPayment.getPaymentStatus());

            Optional<CoCustomer> customer = customerRepository.findById(groupOrderPayment.getCustomerId());
            customer.ifPresent(coCustomer ->
                    groupOrderCustomerPaymentsResponseDto.setCustomerName(
                            coCustomer.getFirstName() + " " + coCustomer.getLastName()));

            gcCustomerPaymentResponseDtoList.add(groupOrderCustomerPaymentsResponseDto);
        }
        groupOrderPaymentDetailsResponseDto.setGroupOrderCustomerPaymentsResponeDtoList(gcCustomerPaymentResponseDtoList);
        return  groupOrderPaymentDetailsResponseDto;
    }

    @Override
    public ResponseEntity<CoResponseDto> placeGroupOrder(PlaceGroupOrderRequestDto placeGroupOrderRequestDto) {
        // 1. Fetch group order
        GroupOrderInvitation invitation = groupOrderInvitationRepository.findById(placeGroupOrderRequestDto.getGroupOrderInvitationId())
                .orElseThrow(() -> new NoSuchElementException("Group order session not found: " + placeGroupOrderRequestDto.getGroupOrderInvitationId()));

        Integer hostId = invitation.getHostCustomerId();
        Integer outletId = invitation.getOutletId();
        Integer goInvitationId = placeGroupOrderRequestDto.getGroupOrderInvitationId();

        // 2. Fetch active members participating in this order sequence
        List<GroupOrderMembers> activeMembers = groupOrderMemberRepository
                .findByGOInvitationIdAndOrderPlaced(goInvitationId,COConstants.GROUP_ORDER_PLACED_TRUE,
                        COConstants.GROUP_ORDER_IS_DROPPED_TRUE);

        if (activeMembers.isEmpty()) {
            log.warn("No active members found with finalized carts for group invitation: {}", goInvitationId);
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CoResponseDto("500",
                    "No active members found with finalized carts for group invitation: "+goInvitationId));
        }

        // 3. Map out which customers belong to which specific delivery addresses
        Map<Integer, List<Integer>> addressToCustomersMap = new HashMap<>();
        for (GroupOrderMembers member : activeMembers) {
            addressToCustomersMap
                    .computeIfAbsent(member.getDeliveryAddressId(), k -> new ArrayList<>())
                    .add(member.getCustomer().getCustomerId());
        }

        // 4. Process each unique delivery address as an independent final order destination
        for (Map.Entry<Integer, List<Integer>> entry : addressToCustomersMap.entrySet()) {
            Integer currentAddressId = entry.getKey();
            List<Integer> customersAtAddress = entry.getValue();

            // Fetch pricing breakdowns matching these exact customers to aggregate totals
            List<GroupOrderPriceBreakup> individualBreakups = priceBreakupRepository
                    .findByGroupOrderInvitationIdAndCustomerIds(goInvitationId, customersAtAddress);

            BigDecimal totalOrderAmount = BigDecimal.ZERO;
            BigDecimal totalDeliveryCharges = BigDecimal.ZERO;
            BigDecimal totalSurgeFee = BigDecimal.ZERO;
            BigDecimal totalPackagingFee = BigDecimal.ZERO;
            BigDecimal totalGst = BigDecimal.ZERO;
            BigDecimal totalOrderAmountWithFees = BigDecimal.ZERO;

            for (GroupOrderPriceBreakup individual : individualBreakups) {
                totalOrderAmount = totalOrderAmount.add(individual.getOrderAmount());
                totalDeliveryCharges = totalDeliveryCharges.add(individual.getDeliverCharges());
                totalSurgeFee = totalSurgeFee.add(individual.getSurgeFee());
                totalPackagingFee = totalPackagingFee.add(individual.getPackagingFee());
                totalGst = totalGst.add(individual.getGst());
                totalOrderAmountWithFees = totalOrderAmountWithFees.add(individual.getOrderTotalAmount());
            }

            // --- B. AGGREGATE AND INSERT INTO JIPPY_CUSTOMER_AND_ORDER.ORDER_ITEMS ---
            CoPlaceOrderRequestDto orderRequestDto = new CoPlaceOrderRequestDto();
            System.out.println("======================"+customersAtAddress.size()+"==="+customersAtAddress);

            orderRequestDto.setGroupOrderInvitationId(goInvitationId);
            orderRequestDto.setCustomerId(customersAtAddress.get(0));

            Optional<CoCustomer> customer = customerRepository.findById(customersAtAddress.get(0));
            if(customer.isPresent()) {
                orderRequestDto.setCustomerPhone(customer.get().getPhoneNumber());
                orderRequestDto.setCreatedBy(customer.get().getCustomerId());
            }

            orderRequestDto.setCustomerDeliveryAddressId(currentAddressId);
            orderRequestDto.setOutletId(outletId);
            orderRequestDto.setOrderType(invitation.getOrderType());
            orderRequestDto.setPaymentModeId(placeGroupOrderRequestDto.getPaymentModeId());
            orderRequestDto.setSurgeFee(totalSurgeFee);
            orderRequestDto.setPackagingFee(totalPackagingFee);
            orderRequestDto.setDeliveryFee(totalDeliveryCharges);
            orderRequestDto.setGst(totalGst);
            orderRequestDto.setOrderAmount(totalOrderAmount);
            orderRequestDto.setCreatedAt(LocalDateTime.now());
            orderRequestDto.setOrderTotalAmount(totalOrderAmountWithFees);

            // Fetch food products added to cart lines by all consumers registered to this location drop point
            List<GroupCartItems> addressCartItems = groupCartItemsRepository
                    .findByGroupOrdersInvitationIdAndCustomerIdIn(goInvitationId, customersAtAddress);

            // Consolidate identical items together into one row if multiple people ordered the same dish
            Map<Integer, CoOrderItemDto> consolidatedItemsMap = new HashMap<>();
            List<CoOrderItemDto> orderItemslist = new ArrayList<>();
            for (GroupCartItems cartLine : addressCartItems) {
                Integer productId = cartLine.getProductId();
                int additionQuantity = cartLine.getQuantity();
                BigDecimal onlineUnitPrice = cartLine.getOnlineUnitPrice();
                BigDecimal merchantUnitPrice = cartLine.getMerchantUnitPrice()!= null ?
                        cartLine.getMerchantUnitPrice() : onlineUnitPrice; // Fallback match protection

                if (consolidatedItemsMap.containsKey(productId)) {
                    CoOrderItemDto existingLine = consolidatedItemsMap.get(productId);
                    int totalQty = existingLine.getQuantity() + additionQuantity;
                    existingLine.setQuantity(totalQty);
                } else {
                    CoOrderItemDto newItem = new CoOrderItemDto();

                    newItem.setProductId(productId);
                    newItem.setQuantity(additionQuantity);
                    newItem.setOnlineUnitPrice(onlineUnitPrice);
                    newItem.setMerchantUnitPrice(merchantUnitPrice);

                    newItem.setCreatedAt(LocalDateTime.now());
                    newItem.setCreatedBy(hostId);

                    consolidatedItemsMap.put(productId, newItem);
                }
            }
            List<CoOrderItemDto> finalItemsList = new ArrayList<>(consolidatedItemsMap.values());
            orderRequestDto.setItems(finalItemsList);
            List<CoOrder> ordersLIst = orderRepository.findByGroupOrderInvitationIdAndCustomerId(goInvitationId,customersAtAddress.get(0));

            if(!(ordersLIst.size() > 0)){
                orderService.placeOrder(orderRequestDto);
            }

            log.info("Successfully generated split branch records for Order ID: {} targeting Destination Address ID: {}",
                    goInvitationId, currentAddressId);
        }
        return  ResponseEntity.status(HttpStatus.OK).body(new CoResponseDto("200",
                "Group Placed placed for invitation id : "+goInvitationId));
    }

    @Override
    public ResponseEntity<?> getActiveGroupOrder(Integer hostCustomerId) {

        Optional<GroupOrderInvitation> groupOrderInvitation = groupOrderInvitationRepository.
                getActiveGroupOrderByCustomerId(hostCustomerId,COConstants.GROUP_ORDER_INVITATION_ACTIVE);

        GroupOrderInvitationDto groupOrderInvitationDto = new GroupOrderInvitationDto();
        if(groupOrderInvitation.isPresent()){
            groupOrderInvitationDto =  GroupOrderMapper.toGroupOrderInvitationResponseDto(groupOrderInvitation.get());
            log.info("Active Group order found with host customer Id :{}, and Group order details {}",
                    hostCustomerId,groupOrderInvitation.get().getOutletId());

            return ResponseEntity.status(HttpStatus.OK).body(groupOrderInvitationDto);
        }else{
            log.info("No active group found with this host customer Id : {}", hostCustomerId);
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active group found with this host customer Id: "+hostCustomerId);
        }
    }


}

