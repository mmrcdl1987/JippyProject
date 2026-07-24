package com.jippy.driver.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.*;
import com.jippy.driver.entity.*;
import com.jippy.driver.exception.DriverZoneException;
import com.jippy.driver.exception.ImageValidationException;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.feignClients.FMFeignClient;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.projection.DriverOrderHistoryProjection;
import com.jippy.driver.projection.DriverTotalEarningsProjection;
import com.jippy.driver.repositary.*;
import com.jippy.driver.service.DriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.locationtech.jts.geom.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverZoneRepository zoneRepository;
    //    private final CoOrderRepository ordersRepository;
    private final COFeignClient coFeignClients;
    private final DriverOrderRepository driverOrderRepository;
    //private final CoOrderRejectionRepository orderRejectionRepository;
    private final DriverIncentiveSettingsRepository driverIncentivesettingsRepository;
    private final DriverIncentiveHistoryRepository driverIncentiveHistoryRepository;
    private final DriverWalletRepository driverWalletRepository;
    private final FMFeignClient fmFeignClient;
    private final DriverLocationService driverLocationService;
    private final S3ImageService s3ImageService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DriverDto postDriverDetails(DriverDto dto) {

        log.info("Creating driver for phone: {}", dto.getPhoneNumber());

        // Convert DTO → Entity
        Driver driver = DriverMapper.mapToDriverEntity(dto);

        // Save driver
        Driver savedDriver = driverRepository.save(driver);

        // for creating user in FM microservice, we will receive the user details from CO microservice and
// then we will save the user details in FM microservice users table
//  --------------------------------------------------------------------------------
        try {
            DriverUserDto userDto = new DriverUserDto();

            userDto.setUsername(savedDriver.getEmail());
            userDto.setPassword(dto.getPassword());
            userDto.setUserId(savedDriver.getDriverId());
            userDto.setUserType(DConstants.TYPE_DRIVER);
            log.info("Creating user in FM for driverId: {}, username: {}", savedDriver.getDriverId(), userDto.getUsername());
            fmFeignClient.createUser(userDto);

            log.info("User created in FM for driverId: {}", savedDriver.getDriverId());

        } catch (Exception e) {
            log.error("User creation failed in FM", e);
        }

//        ----------------------------------------------------------------------
        /** CALLING HELPER METHOD
         * Create Approval Request in Food & Mart Microservice.
         */
          createApprovalRequest(savedDriver.getDriverId());
//          -------------------------------------------------------------------

//        // Fetch role
//        FmRoles role = roleRepository.findByRoleName(DConstants.ROLE_DRIVER);
//        if (role == null) {
//            throw new RuntimeException("Role not found");
//        }
//        //  Fetch role_permissions
//        List<FmRolePermissions> rolePermissionsList = rolePermissionsRepository.findByRole(role);
//
//        if (rolePermissionsList.isEmpty()) {
//            throw new RuntimeException("No permissions mapped to role");
//        }
//
//        //  Map user → role_permissions
//        for (FmRolePermissions rp : rolePermissionsList) {
//
//            FmUserRolePermissions urp = FmMerchantMapper.toUserRolesEntity(users, rp);
//
//            userRolesRepository.save(urp);
//        }
//---------------------------------------------------------------------------------------------

        log.info("Driver saved with id: {}", savedDriver.getDriverId());

        DriverAddressRequestDto coAddressRequestDto = new DriverAddressRequestDto();
        coAddressRequestDto.setJippyAddressId(savedDriver.getDriverId());
        coAddressRequestDto.setBuildingNumber(dto.getBuildingNumber());
        coAddressRequestDto.setRoad(dto.getRoad());
        coAddressRequestDto.setLandmark(dto.getLandmark());
        coAddressRequestDto.setCityId(dto.getCityId());
        coAddressRequestDto.setStateId(dto.getStateId());
        coAddressRequestDto.setAreaId(dto.getAreaId());
        coAddressRequestDto.setAddressType(DConstants.TYPE_DRIVER);
        DriverAddressRequestDto coAddressRequestDtoFeign = null;
        try {
            coAddressRequestDtoFeign = fmFeignClient.saveAddressDetails(coAddressRequestDto).getBody();
        } catch (Exception e) {
            log.error("Address service failed, but continuing driver creation", e);
        }
//        create driver wallet details , create entity ,repo
        DriverWallet wallet = new DriverWallet();

        wallet.setDriverId(savedDriver.getDriverId()); // FROM DRIVER TABLE

//        from CO constants default values
//        new requirement : set default cod amount and orders lock for new driver
        wallet.setTotalCodAmount(DConstants.DRIVER_DEFAULT_COD_AMOUNT); // default 1000
        wallet.setOrdersLock(DConstants.DRIVER_ORDERS_LOCK); // default true

        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setCreatedBy(savedDriver.getDriverId());

        driverWalletRepository.save(wallet);

        // Convert Entity → DTO
        DriverDto mapToDriverDto = DriverMapper.mapToDriverDto(savedDriver, coAddressRequestDtoFeign);

        return mapToDriverDto;
    }
//    ----------------------------------------------------------------------------------------------

    /**------  HELPER METHOD - For Approval Request
     * Creates an Approval Request in Food & Mart Microservice.
     *
     * Every newly created Driver enters the approval workflow
     * at Level 1 with PENDING status.
     *
     * @param driverId Newly created Driver Id.
     */
    private void createApprovalRequest(Integer driverId) {

        try {
            DriverApprovalRequestDTO requestDTO = new DriverApprovalRequestDTO();

            requestDTO.setEntityType(DConstants.TYPE_DRIVER);
            requestDTO.setEntityId(driverId);
            requestDTO.setCreatedBy(driverId);

            log.info("Creating Approval Request for Driver Id: {}", driverId);

            fmFeignClient.createApprovalRequest(requestDTO);

            log.info("Approval Request created successfully for Driver Id: {}", driverId);

        } catch (Exception ex) {

            log.error("Failed to create Approval Request for Driver Id: {}", driverId, ex);
        }
    }
//    ---------------------------------------------------------------------------------------------

    @Override
    @Transactional
    public DriverDto getDriverDetails(Integer driverId) {

        log.info("Fetching driver with id: {}", driverId);

        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> {
            log.error("Driver not found with id: {}", driverId);
            return new ResourceNotFoundException("Driver not found with id: " + driverId);
        });

        DriverAddressRequestDto address = null;

        try {
            address = fmFeignClient.getAddressDetails(driverId).getBody();
        } catch (Exception e) {
            log.error("Failed to fetch address from FM", e);
        }

        return DriverMapper.mapToDriverDto(driver, address);
    }

    //    updating driver details, only editable fields (not phone, email, or KYC)
//    and address details through feign client
    @Override
    @Transactional
    public DriverDto updateDriverDetails(Integer driverId, DriverDto dto) {

        log.info("Updating driver with id: {}", driverId);

        // Fetch existing driver from DB
        Driver existingDriver = driverRepository.findById(driverId).orElseThrow(() -> {
            log.error("Driver not found with id: {}", driverId);

            return new ResourceNotFoundException("Driver not found with id: " + driverId);
        });

        // Update only editable fields using mapper
        DriverMapper.updateDriverEntity(existingDriver, dto);

        // Save updated driver to entity
        Driver updatedDriver = driverRepository.save(existingDriver);

        log.info("Driver updated successfully with id: {}", driverId);

        // Update address through feign client
        DriverAddressRequestDto addressDto = new DriverAddressRequestDto();

        addressDto.setJippyAddressId(driverId);
        addressDto.setBuildingNumber(dto.getBuildingNumber());
        addressDto.setRoad(dto.getRoad());
        addressDto.setLandmark(dto.getLandmark());
        addressDto.setCityId(dto.getCityId());
        addressDto.setStateId(dto.getStateId());
        addressDto.setAreaId(dto.getAreaId());
        addressDto.setAddressType(DConstants.TYPE_DRIVER);

        // Save/update address
        DriverAddressRequestDto updatedAddress = null;

        try {
            updatedAddress = fmFeignClient.saveAddressDetails(addressDto).getBody();
        } catch (Exception e) {
            log.error("Address update failed", e);
        }
        // Convert updated entity → response DTO with updated address details
        DriverDto response = DriverMapper.mapToDriverDto(updatedDriver, updatedAddress);

        return response;
    }


    //    to fetch driver earnings for a given date, default to current date if not provided,
//    and total orders count for that day, and total earnings for that day
    @Override
    @Transactional
    public DriverEarningsDto fetchEarnings(Integer driverId, LocalDate date) {

        log.info("FETCH_EARNINGS_API_START | driverId={} | date={}",
                driverId,
                date);

        // ------------------------------------------------------------------
        // Validate driver exists
        // ------------------------------------------------------------------
        driverRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("DRIVER_NOT_FOUND | driverId={}", driverId);
                    return new ResourceNotFoundException(
                            "Driver not found with id: " + driverId);
                });

        // ------------------------------------------------------------------
        // Fetch total earnings and completed orders count from CO microservice
        // ------------------------------------------------------------------
        DriverEarningsDto projectionOfTotalEarningsAndCountOfOrders =
                coFeignClients.fetchDriverEarnings(driverId, date);

        log.info("EARNINGS_FETCHED_FROM_CO | driverId={} | totalEarnings={} | ordersCount={}",
                driverId,
                projectionOfTotalEarningsAndCountOfOrders.getTotalEarningsToday(),
                projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday());

        // ------------------------------------------------------------------
        // Prepare response DTO
        // ------------------------------------------------------------------
        DriverEarningsDto driverEarningsDto = new DriverEarningsDto();

        driverEarningsDto.setDriverId(driverId);
        driverEarningsDto.setCurrentDate(date);

        driverEarningsDto.setOrdersCountToday(
                projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday());

        driverEarningsDto.setTotalEarningsToday(
                projectionOfTotalEarningsAndCountOfOrders.getTotalEarningsToday());

        // ------------------------------------------------------------------
        // Fetch incentive slabs and calculate incentive bonus
        // ------------------------------------------------------------------
        List<DriverIncentiveSettings> slabs =
                driverIncentivesettingsRepository.findAllSlabs();

        Integer orders =
                projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday() != null
                        ? projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday().intValue()
                        : 0;

        log.info("TOTAL_COMPLETED_ORDERS | driverId={} | orders={}",
                driverId,
                orders);

        // ------------------------------------------------------------------
// Check minimum eligible orders for incentive
// If orders are less than minimum slab,
// return response and do not save incentive history
// ------------------------------------------------------------------
//        check if slabs are configured or not in table, if not configured
//        then return response with 0 incentive bonus without saving incentive history
        if (slabs.isEmpty()) {
            log.warn("No incentive slabs configured");

            driverEarningsDto.setDriverIncentiveBonus(BigDecimal.ZERO);
            return driverEarningsDto;
        }
        Integer minimumEligibleOrders = slabs.get(0).getOrdersCount();

        log.info("MINIMUM_ELIGIBLE_ORDERS | {}", minimumEligibleOrders);

        if (orders < minimumEligibleOrders) {

            log.info(
                    "DRIVER_NOT_ELIGIBLE_FOR_INCENTIVE | driverId={} | orders={} | minimumRequired={}",
                    driverId,
                    orders,
                    minimumEligibleOrders);

            driverEarningsDto.setDriverIncentiveBonus(BigDecimal.ZERO);

            log.info(
                    "FETCH_EARNINGS_API_SUCCESS | driverId={} | orders={} | totalEarnings={} | incentiveBonus=0",
                    driverId,
                    driverEarningsDto.getOrdersCountToday(),
                    driverEarningsDto.getTotalEarningsToday());

            return driverEarningsDto;
        }

        // ------------------------------------------------------------------
        // Calculate applicable incentive slab using mapper
        // ------------------------------------------------------------------
        BigDecimal bonus =
                DriverMapper.calculateIncentiveBonus(slabs, orders);

        log.info("INCENTIVE_BONUS_CALCULATED | driverId={} | bonus={}",
                driverId,
                bonus);

        // ------------------------------------------------------------------
        // Check whether incentive history exists for driver and date
        // ------------------------------------------------------------------
        Optional<DriverIncentiveHistory> existingHistory =
                driverIncentiveHistoryRepository
                        .findByDriverIdAndCurrDate(driverId, date);


        DriverIncentiveHistory history =
                DriverMapper.mapToDriverIncentiveHistory(
                        existingHistory.orElse(null),
                        driverId,
                        date,
                        orders,
                        bonus);
        // ------------------------------------------------------------------
        // Save incentive history
        // ------------------------------------------------------------------
        history = driverIncentiveHistoryRepository.save(history);

        log.info("INCENTIVE_HISTORY_SAVED | historyId={} | driverId={} | incentiveAmount={}",
                history.getDriverIncentiveHistoryId(),
                driverId,
                history.getIncentiveAmount());

        // ------------------------------------------------------------------
        // Set incentive amount from history table into response DTO
        // ------------------------------------------------------------------
        driverEarningsDto.setDriverIncentiveBonus(
                history.getIncentiveAmount()
        );

        log.info("FETCH_EARNINGS_API_SUCCESS | driverId={} | orders={} | totalEarnings={} | incentiveBonus={}",
                driverId,
                driverEarningsDto.getOrdersCountToday(),
                driverEarningsDto.getTotalEarningsToday(),
                driverEarningsDto.getDriverIncentiveBonus());

        return driverEarningsDto;
    }

    // for a given driver, fetch order earnings history with details like order id, pick up and delivery distance,
    // charges, total fee, surge fee, tips, order status, and outlet name for each order
//    @Override
//    public List<DriverOrderHistoryDto> fetchOrderEarningsHistory(Integer driverId) {
//
//        log.info("Fetching order earnings history for driver id: {}", driverId);
//
//        // Check driver exists or not
//        driverRepository.findById(driverId).orElseThrow(() -> {
//
//            log.error("Driver not found with id: {}", driverId);
//
//            return new ResourceNotFoundException("Driver not found with id: " + driverId);
//        });
//
//        // Fetch records from database
//        List<DriverOrderHistoryProjection> projections = driverOrderRepository.fetchOrderEarningsHistory(driverId);
//
//        // Create response list to hold order history details
//        List<DriverOrderHistoryDto> ProjectionResponse = new ArrayList<>();
//
//        // Loop through all records and set values in response DTO,
//        // also fetch outlet name from FM microservice for each record
//        for (DriverOrderHistoryProjection projection : projections) {
//
//            // Fetch outlet name from FM microservice
//            String FmOutletName = fmFeignClient.fetchOutletName(projection.getOutletId());
//
//            // Convert projection → DTO using mapper
//            DriverOrderHistoryDto dto = DriverMapper.mapToDriverOrderHistoryDto(projection, FmOutletName);
//
//            // Add DTO to response list
//            ProjectionResponse.add(dto);
//        }
//
//        log.info("Successfully fetched order earnings history for driver id: {}", driverId);
//
//        return ProjectionResponse;
//    }
    @Override
    public List<DriverOrderHistoryDto> fetchOrderEarningsHistory(Integer driverId) {

        log.info("Fetching order earnings history for driver id: {}", driverId);

        driverRepository.findById(driverId).orElseThrow(() -> {

            log.error("Driver not found with id: {}", driverId);

            return new ResourceNotFoundException("Driver not found with id: " + driverId);
        });

        List<DriverOrderHistoryProjection> projections = driverOrderRepository.fetchOrderEarningsHistory(driverId);

        List<DriverOrderHistoryDto> response = new ArrayList<>();

        for (DriverOrderHistoryProjection projection : projections) {

            // call customer ms
            DriveOrderDto order = coFeignClients.getOrder(String.valueOf(projection.getOrderId()));

            log.info("Order Response = {}", order);
            // call fm ms
            log.info("Calling FM with outletId={}", order.getOutletId());

            String outletName = fmFeignClient.fetchOutletName(order.getOutletId());

            log.info("Outlet Name={}", outletName);

            DriverOrderHistoryDto dto = DriverMapper.mapToDriverOrderHistoryDto(projection, order.getOrderStatus(), outletName);

            response.add(dto);
        }

        log.info("Successfully fetched order earnings history for driver id: {}", driverId);

        return response;
    }

    @Override
    public DriverTotalEarningsDto fetchTotalEarnings(Integer driverId) {

        log.info("Fetching total earnings for driver id: {}", driverId);

        // Validate driver exists
        driverRepository.findById(driverId).orElseThrow(() -> {

            log.error("Driver not found with id: {}", driverId);

            return new ResourceNotFoundException("Driver not found with id: " + driverId);
        });

        // Fetch earnings data
        DriverTotalEarningsProjection projection = driverOrderRepository.fetchTotalEarnings(driverId);

        // Fetch rejected orders count
        // Long rejectedOrders = orderRejectionRepository.fetchRejectedOrdersCount(driverId);
        Long rejectedOrders = coFeignClients.fetchRejectedOrdersCount(driverId);
        // Convert to DTO using mapper
        DriverTotalEarningsDto response = DriverMapper.mapToTotalEarningsDto(driverId, projection, rejectedOrders);

        log.info("Successfully fetched total earnings for driver id: {}", driverId);

        return response;
    }

    @Transactional
    @Override
    public String createZones(DriverZoneDto zoneDto) {

        Optional<DriverZone> existingZone = zoneRepository.findByZoneName(zoneDto.getZoneName());
        MultiPolygon multiPolygon = convertToJtsPolygon(zoneDto.getBoundary());
        //Polygon polygon = convertToJtsPolygon(zoneDto.getBoundary());
        if (existingZone.isPresent()) {
            if (zoneRepository.existsBySpatialBoundary(multiPolygon)) {
                throw new DriverZoneException("A boundary with this exact shape already exists!");
            } else {
                log.info("Updating existing zone with id: {}", existingZone.get().getZoneId());
                DriverZone zoneToUpdate = existingZone.get();
                zoneToUpdate.setBoundary(multiPolygon);
                zoneToUpdate.setUpdatedAt(LocalDateTime.now());
                zoneToUpdate.setUpdatedBy(zoneDto.getCreatedBy());
                zoneToUpdate.setZoneType(zoneDto.getZoneType());
                zoneRepository.save(zoneToUpdate);
                return "Zone:" + zoneToUpdate.getZoneName() + " updated successfully!";
            }
        }
        DriverZone zone = DriverMapper.mapToZoneEntity(zoneDto, multiPolygon);
        DriverZone savedZone = zoneRepository.save(zone);
        log.info("New zone is created with id: {}", savedZone.getZoneId());

        return "Zone:" + zone.getZoneName() + " created successfully!";
    }

    private MultiPolygon convertToJtsPolygon(List<List<List<DriverZoneDto.CoordinateDTO>>> boundary) {
        List<Polygon> polygonsList = new ArrayList<>();

        // Loop 1: Iterate through each independent Polygon shape
        for (List<List<DriverZoneDto.CoordinateDTO>> rawPolygon : boundary) {

            // In a standard geometry, index 0 is always the outer boundary ring
            List<DriverZoneDto.CoordinateDTO> exteriorRingCoords = rawPolygon.get(0);

            // Map CoordinateDTO to JTS Coordinate objects
            Coordinate[] coordinates = exteriorRingCoords.stream()
                    .map(c -> new Coordinate(c.getLongitude(), c.getLatitude()))
                    .toArray(Coordinate[]::new);

            // Create the closed linear ring for this polygon
            LinearRing exteriorRing = geometryFactory.createLinearRing(coordinates);

            // Create a polygon from the ring (passing null since we aren't handling internal holes right now)
            Polygon polygon = geometryFactory.createPolygon(exteriorRing, null);
            polygonsList.add(polygon);
        }

        // Convert our list of individual polygons into a native array
        Polygon[] polygonArray = polygonsList.toArray(Polygon[]::new);

        // Combine everything into a single MultiPolygon object
        MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygonArray);

        // Match your database spatial SRID coordinate system reference alignment
        multiPolygon.setSRID(4326);

        return  multiPolygon;
    }

//    @Override
//    public String driverDeliveredOrder(DriverOrderDto driverOrderDto) {
//        log.info("Processing driver delivered order for driver id: {} and order id: {}",
//                driverOrderDto.getDriverId(), driverOrderDto.getOrderId());
//
//        // Validate driver exists
//        Driver driver = driverRepository.findById(driverOrderDto.getDriverId()).orElseThrow(() -> {
//
//            log.error("Driver not found with id: {}", driverOrderDto.getDriverId());
//
//            return new ResourceNotFoundException("Driver not found with id: " + driverOrderDto.getDriverId());
//        });
//
//        // Validate order exists and belongs to the driver
//        CoOrder order = ordersRepository.findById(driverOrderDto.getOrderId()).orElseThrow(() -> {
//
//            log.error("Order not found with id: {}", driverOrderDto.getOrderId());
//
//            return new ResourceNotFoundException("Order not found with id: " + driverOrderDto.getOrderId());
//        });
//
//        if (!order.getDriverId().equals(driverOrderDto.getDriverId())) {
//            log.error("Order with id: {} does not belong to driver with id: {}",
//                    driverOrderDto.getOrderId(), driverOrderDto.getDriverId());
//            throw new ResourceNotFoundException("Order with id: " + driverOrderDto.getOrderId() +
//                    " does not belong to driver with id: " + driverOrderDto.getDriverId());
//        }
//
//        Optional<DriverOrder> driverOrder = driverOrderRepository.findByDriverIdOrderId(driverOrderDto.getDriverId(), driverOrderDto.getOrderId());
//        if(driverOrder.isPresent()){
//            log.error("Driver order already exists for driver id: {} and order id: {}",
//                    driverOrderDto.getDriverId(), driverOrderDto.getOrderId());
//            throw new IllegalArgumentException("Driver order already exists for driver id: " + driverOrderDto.getDriverId() +
//                    " and order id: " + driverOrderDto.getOrderId());
//        }
//
//        // Update order status to delivered
//        order.setOrderStatus(DConstants.STATUS_DELIVERED);
//        ordersRepository.save(order);
//
//        // Update earnings details in driver_orders table
//        DriverOrder newDriverOrder = DriverMapper.mapToDriverOrderEntity(driverOrderDto, driver);
//        DriverOrder savedDriverOrder = driverOrderRepository.save(newDriverOrder);
//
//        driverLocationService.pushCompleteOrderEvent(driverOrderDto);
//
//        log.info("Successfully processed delivered order for order id: {} and updated earnings for driver id: {}", driverOrderDto.getOrderId(), driverOrderDto.getDriverId());
//        return "Order with id: " + driverOrderDto.getOrderId() + " marked as delivered and earnings updated for driver with id: " + driverOrderDto.getDriverId();
//    }

    @Override
    public String driverDeliveredOrder(DriverOrderDto driverOrderDto) {

        log.info("Processing driver delivered order for driver id: {} and order id: {}", driverOrderDto.getDriverId(), driverOrderDto.getOrderId());

        // Validate driver exists
        Driver driver = driverRepository.findById(driverOrderDto.getDriverId()).orElseThrow(() -> {

            log.error("Driver not found with id: {}", driverOrderDto.getDriverId());

            return new ResourceNotFoundException("Driver not found with id: " + driverOrderDto.getDriverId());
        });

        // Check already processed or not
        Optional<DriverOrder> driverOrder = driverOrderRepository.findByDriverIdOrderId(driverOrderDto.getDriverId(), driverOrderDto.getOrderId());

        if (driverOrder.isPresent()) {

            log.error("Driver order already exists for driver id: {} and order id: {}", driverOrderDto.getDriverId(), driverOrderDto.getOrderId());

            throw new IllegalArgumentException("Driver order already exists for driver id: " + driverOrderDto.getDriverId() + " and order id: " + driverOrderDto.getOrderId());
        }

        // Call customerandorder microservice
        String response = coFeignClients.deliverOrder(driverOrderDto.getOrderId(), driverOrderDto.getDriverId());

        log.info("CustomerAndOrder response: {}", response);

        // Save earnings details in driver_orders table
        DriverOrder newDriverOrder = DriverMapper.mapToDriverOrderEntity(driverOrderDto, driver);

        driverOrderRepository.save(newDriverOrder);

        // Push kafka event
        driverLocationService.pushCompleteOrderEvent(driverOrderDto);

        log.info("Successfully processed delivered order for order id: {} and updated earnings for driver id: {}", driverOrderDto.getOrderId(), driverOrderDto.getDriverId());

        return "Order with id: " + driverOrderDto.getOrderId() + " marked as delivered and earnings updated for driver with id: " + driverOrderDto.getDriverId();
    }

    @Override
    public String saveOrUpdateProfilePic(UploadProfilePicDto uploadProfilePicDto) {
        try {
            validateImage(uploadProfilePicDto.getProfilePicFile());
            String s3BucketFilePath = s3ImageService.uploadFile(uploadProfilePicDto.getProfilePicFile(), uploadProfilePicDto.getUserId(), uploadProfilePicDto.getUserType());
            log.info("File uploaded to S3 successfully. Bucket path: {}", s3BucketFilePath);

            if (uploadProfilePicDto.getUserType().equalsIgnoreCase(DConstants.TYPE_DRIVER)) {

                Driver driver = driverRepository.findById(uploadProfilePicDto.getUserId()).orElseThrow(() -> {
                    log.error("Driver not found with id: {}", uploadProfilePicDto.getUserId());
                    return new ResourceNotFoundException("Driver not found with id: " + uploadProfilePicDto.getUserId());
                });

                driver.setProfilePicUrl(DConstants.AWS_PROFILE_PIC_STATIC_URL + s3BucketFilePath);
                driverRepository.save(driver);
                log.info("Driver profile picture URL updated in database for driver id: {}", uploadProfilePicDto.getUserId());

                return "Driver profile pic Url: " + DConstants.AWS_PROFILE_PIC_STATIC_URL + s3BucketFilePath;
            } else if (uploadProfilePicDto.getUserType().equalsIgnoreCase(DConstants.TYPE_CUSTOMER)) {
                DriverCustomerResponseDto driverCustomerResponseDto = coFeignClients.getCustomer(uploadProfilePicDto.getUserId()).getBody();

                if (driverCustomerResponseDto.getCustomerId() != null) {

                    driverCustomerResponseDto.setProfilePicUrl(DConstants.AWS_PROFILE_PIC_STATIC_URL + s3BucketFilePath);

                    ResponseEntity<DriverResponseDto> dtoResponseEntity = coFeignClients.updateCustomerProfilePic(driverCustomerResponseDto);
                    log.info("Customer profile picture URL updated in database for customer id: {}", uploadProfilePicDto.getUserId());

                    return dtoResponseEntity.getBody().getStatusMsg();
                } else {
                    log.error("Customer not found with id: {}", uploadProfilePicDto.getUserId());
                    throw new ResourceNotFoundException("Customer not found with id: " + uploadProfilePicDto.getUserId());
                }
            } else if (uploadProfilePicDto.getUserType().equalsIgnoreCase(DConstants.TYPE_MERCHANT)) {

                DriverMerchantDto driverMerchantDto = fmFeignClient.getMerchantById(uploadProfilePicDto.getUserId()).getBody();

                if (driverMerchantDto.getMerchantId() != null) {
                    driverMerchantDto.setProfilePicUrl(DConstants.AWS_PROFILE_PIC_STATIC_URL + s3BucketFilePath);
                    ResponseEntity<DriverResponseDto> driverResponseDto = fmFeignClient.updateMerchantProfilePic(driverMerchantDto);
                    log.info("Merchant profile picture URL updated in database for merchant id: {}", uploadProfilePicDto.getUserId());

                    return driverResponseDto.getBody().getStatusMsg();
                } else {
                    log.error("Customer not found with id: {}", uploadProfilePicDto.getUserId());
                    throw new ResourceNotFoundException("Merchant not found with id: " + uploadProfilePicDto.getUserId());
                }
            }
        } catch (IOException e) {
            log.error("Error validating image file", e);
            throw new ImageValidationException("Error validating image file: " + e.getMessage());
        }
        return "";
    }

    public void validateImage(MultipartFile file) throws IOException {
        // 1. Check if file is empty
        if (file.isEmpty()) {
            throw new ImageValidationException("File cannot be empty");
        }

        // 2. Validate File Type (MIME Type)
        String contentType = file.getContentType();
        if (!isValidType(contentType)) {
            throw new ImageValidationException("Only PNG, JPEG, and JPG are allowed");
        }

        // 3. Validate File Size (e.g., Max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new ImageValidationException("File size exceeds the 5MB limit");
        }

        //4. Validate Image Dimensions (e.g., Min 200x200 pixels)
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new ImageValidationException("Invalid image file");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width < 200 || height < 200) {
            throw new ImageValidationException("Image must be at least 200x200 pixels");
        }
    }

    private boolean isValidType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"));
    }

    @Override
    public DriverDto findByEmail(String email) {

        Driver driver = driverRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with email : " + email));

        DriverDto dto = new DriverDto();

        dto.setDriverId(driver.getDriverId());
        dto.setEmail(driver.getEmail());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());

        return dto;
    }
//    -----------------------------For Driver Approvals Level 1----------------------------------------------------------------
    @Override
    public FmDriverApprovalResponseDTO getDriverById(Integer driverId) {

    Driver driver = driverRepository.findByDriverId(driverId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Driver not found with Id : " + driverId));

    return DriverMapper.mapToDriverApprovalResponseDto(driver);
    }

    @Override
    public List<DriverZoneResponseDto> getZonesByType(String zoneType) {
        List<Map<String, Object>> rawRows = zoneRepository.findZonesByType(zoneType.toUpperCase());
        List<DriverZoneResponseDto> responseList = new ArrayList<>();

        for (Map<String, Object> row : rawRows) {
            try {
                Integer id = (Integer) row.get("zone_id");
                String name = (String) row.get("zone_name");
                String type = (String) row.get("zone_type");
                String geoJsonStr = (String) row.get("boundary_json");

                // Convert stringified GeoJSON from PostGIS into a real JSON Node object
                var boundaryNode = objectMapper.readTree(geoJsonStr);

                responseList.add(new DriverZoneResponseDto(id, name, type, boundaryNode));
            } catch (Exception e) {
                // Log exception (e.g., JSON parsing failure)
                throw new RuntimeException("Failed to parse spatial boundary data", e);
            }
        }
        return responseList;
    }

    @Override
    public DriverZoneResponseDto findCommunityById(Integer communityId) {

        Optional<DriverZone> driverZone = zoneRepository.findByZoneIdAndZoneType(communityId, DConstants.COMMUNITY_TYPE);
        DriverZoneResponseDto driverZoneResponseDto = new DriverZoneResponseDto();

        if (driverZone.isPresent()) {
            driverZoneResponseDto.setZoneId(driverZone.get().getZoneId());
            driverZoneResponseDto.setZoneType(driverZone.get().getZoneType());
            driverZoneResponseDto.setZoneName(driverZone.get().getZoneName());

            return driverZoneResponseDto;
        }
        return driverZoneResponseDto;
    }

    @Override
    public Integer findCustomerInCommunity(Double latitude, Double longitude) {

        Optional<DriverZone> driverZone = zoneRepository.findCustomerInCommunity(latitude,longitude);
        if (driverZone.isPresent()) {

            return driverZone.get().getZoneId();
        }
        return 0;
    }

    @Override
    public Integer checkCustomerAddressWithCommunity(Double latitude, Double longitude,Integer communityId) {

        Optional<DriverZone> driverZone = zoneRepository.checkCustomerAddressWithCommunity(latitude,longitude,communityId);
        if (driverZone.isPresent()) {

            return driverZone.get().getZoneId();
        }
        return 0;
    }

}