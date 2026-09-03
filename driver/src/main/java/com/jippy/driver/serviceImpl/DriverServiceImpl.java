package com.jippy.driver.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.*;
import com.jippy.driver.entity.*;
import com.jippy.driver.exception.DriverBusinessException;
import com.jippy.driver.exception.DriverZoneException;
import com.jippy.driver.exception.ImageValidationException;
import com.jippy.driver.exception.ResourceNotFoundException;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.feignClients.FMFeignClient;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.projection.DriverOrderHistoryProjection;
import com.jippy.driver.projection.DriverTotalEarningsProjection;
import com.jippy.driver.repositary.*;
import com.jippy.driver.service.DriverService;
import com.jippy.driver.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final DriverKycRepository driverKycRepository;
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
    private final EmailService emailService;

    @Override
    @Transactional
    public DriverDto postDriverDetails(DriverDto dto) {

        log.info("Creating driver for phone: {}", dto.getPhoneNumber());
        // ----------------------------------------------------------------------
        // Validate duplicate phone number
        // ----------------------------------------------------------------------
        if (driverRepository.existsByPhoneNumber(dto.getPhoneNumber())) {

            log.error("Phone number already exists : {}", dto.getPhoneNumber());

            throw new DriverBusinessException(
                    "Phone number already exists.");
        }

// ----------------------------------------------------------------------
// Validate duplicate email
// ----------------------------------------------------------------------
        if (driverRepository.existsByEmail(dto.getEmail())) {

            log.error("Email already exists : {}", dto.getEmail());

            throw new DriverBusinessException(
                    "Email already exists.");
        }

// ----------------------------------------------------------------------
// Validate duplicate Aadhaar number
// ----------------------------------------------------------------------
        if (driverKycRepository.existsByAadharNumber(dto.getAadharNumber())) {

            log.error("Aadhaar number already exists : {}",
                    dto.getAadharNumber());

            throw new DriverBusinessException(
                    "Aadhaar number already exists.");
        }

// ----------------------------------------------------------------------
// Validate duplicate Driving License Number
// ----------------------------------------------------------------------
        if (driverKycRepository.existsByDrivingLicenseNumber(
                dto.getDrivingLicenseNumber())) {

            log.error("Driving License already exists : {}",
                    dto.getDrivingLicenseNumber());

            throw new DriverBusinessException(
                    "Driving License number already exists.");
        }

// ----------------------------------------------------------------------
// Validate duplicate RC Copy
// ----------------------------------------------------------------------
        if (driverKycRepository.existsByRcCopy(dto.getRcCopy())) {

            log.error("RC Copy already exists : {}",
                    dto.getRcCopy());

            throw new DriverBusinessException(
                    "RC Copy already exists.");
        }

        // Convert DTO → Entity
        Driver driver = DriverMapper.mapToDriverEntity(dto);


        Driver savedDriver = driverRepository.save(driver);

        log.info(
                "Driver saved with id: {}",
                savedDriver.getDriverId()
        );


        log.info(
                "Driver Registration Email Sent Successfully. Driver Id : {}, Email : {}",
                savedDriver.getDriverId(),
                savedDriver.getEmail()
        );

        // for creating user in FM microservice, we will receive the user details from CO microservice and
// then we will save the user details in FM microservice users table
//  --------------------------------------------------------------------------------
        try {

            DriverUserDto userDto = new DriverUserDto();

            userDto.setUsername(savedDriver.getEmail());
            userDto.setPassword(dto.getPassword());
            userDto.setUserId(savedDriver.getDriverId());
            userDto.setUserType(DConstants.TYPE_DRIVER);

            log.info("Calling FM createUser: {}", userDto);

            ResponseEntity<DriverUserDto> response =
                    fmFeignClient.createUser(userDto);

            log.info(
                    "FM createUser response: status={}, body={}",
                    response.getStatusCode(),
                    response.getBody()
            );

        } catch (feign.FeignException e) {

            log.error(
                    "FM createUser FAILED: status={}, responseBody={}",
                    e.status(),
                    e.contentUTF8(),
                    e
            );

        } catch (Exception e) {

            log.error("FM createUser FAILED", e);
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
        coAddressRequestDto.setLatitude(dto.getLatitude());
        coAddressRequestDto.setLongitude(dto.getLongitude());
        DriverAddressRequestDto coAddressRequestDtoFeign = null;
        try {

            coAddressRequestDtoFeign =
                    fmFeignClient.saveAddressDetails(coAddressRequestDto).getBody();

        } catch (Exception e) {

            log.error(
                    "ADDRESS FEIGN FAILED | driverId={} | errorType={} | message={}",
                    savedDriver.getDriverId(),
                    e.getClass().getName(),
                    e.getMessage(),
                    e
            );

            throw new DriverBusinessException(
                    "Failed to create driver address: " + e.getMessage()
            );
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
        DriverDto mapToDriverDto =
                DriverMapper.mapToDriverDto(savedDriver, coAddressRequestDtoFeign);

        emailService.sendDriverRegistrationEmail(
                savedDriver.getEmail(),
                savedDriver.getFirstName() + " " + savedDriver.getLastName()
        );

        return mapToDriverDto;
    }
//    ----------------------------------------------------------------------------------------------

    /**
     * ------  HELPER METHOD - For Approval Request
     * Creates an Approval Request in Food & Mart Microservice.
     * <p>
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
//=========================================================================================
//=========================================================================================

    @Override
    @Transactional
    public DriverDto getDriverDetails(Integer driverId) {

        log.info("Fetching driver with id: {}", driverId);

        // -----------------------------------------
        // 1. Get Driver + KYC from Driver MS
        // -----------------------------------------
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Driver not found with id: {}", driverId);
                    return new ResourceNotFoundException(
                            "Driver not found with id: " + driverId
                    );
                });

        // -----------------------------------------
        // 2. Get Address from FM
        // -----------------------------------------
        DriverAddressRequestDto address = null;

        try {

            address = fmFeignClient.getAddressDetails(driverId).getBody();

        } catch (Exception e) {

            log.error("Failed to fetch address from FM for driverId: {}",
                    driverId,
                    e
            );
        }

        // -----------------------------------------
        // 3. Get User Status from FM
        // -----------------------------------------
        DriverUserDto user = null;

        try {

            user = fmFeignClient.findByUserIdAndUserType(
                    driverId,
                    DConstants.TYPE_DRIVER).getBody();

            log.info("========== FM USER RESPONSE ==========");
            log.info("User ID     : {}", user != null ? user.getUserId() : null);
            log.info("Username    : {}", user != null ? user.getUsername() : null);
            log.info("User Type   : {}", user != null ? user.getUserType() : null);
            log.info("Is Active   : {}", user != null ? user.getIsActive() : null);
            log.info("======================================");

        } catch (Exception e) {

            log.error(
                    "Failed to fetch user details from FM for driverId: {}",
                    driverId,
                    e
            );
        }

        // -----------------------------------------
        // 4. Combine everything
        // -----------------------------------------
        return DriverMapper.mapToDriverDto(driver, address, user);
    }

    @Override
    @Transactional
    public List<DriverDto> getAllDrivers() {

        log.info("GET_ALL_DRIVERS_API_START");

        List<Driver> drivers = driverRepository.findAll();

        if (drivers.isEmpty()) {
            log.info("NO_DRIVERS_FOUND");
            return new ArrayList<>();
        }

        List<DriverDto> response = new ArrayList<>();

        for (Driver driver : drivers) {

            DriverAddressRequestDto address = null;

            try {

                log.info(
                        "FETCHING_DRIVER_ADDRESS | driverId={}",
                        driver.getDriverId());

                address = fmFeignClient
                        .getAddressDetails(driver.getDriverId())
                        .getBody();

            } catch (Exception e) {

                log.error(
                        "FAILED_TO_FETCH_DRIVER_ADDRESS | driverId={}",
                        driver.getDriverId(),
                        e);
            }

            DriverDto dto = DriverMapper.mapToDriverListDto(
                    driver,
                    address
            );

            response.add(dto);
        }

        log.info(
                "GET_ALL_DRIVERS_API_SUCCESS | totalDrivers={}",
                response.size());

        return response;
    }

    /**
     * Approves the driver.
     */
    @Override
    public void approveDriver(Integer driverId) {

        log.info("Started Driver Approval. Driver Id : {}", driverId);

        // Validate and fetch Driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> {

                    log.error("Driver Not Found. Driver Id : {}", driverId);

                    return new ResourceNotFoundException(
                            "Driver Not Found : " + driverId);
                });

        // Update approval status
        driverRepository.approveDriver(driverId);

        log.info(
                "Driver Approval Completed Successfully. Driver Id : {}",
                driverId
        );

        // Send Driver Approval Email
        emailService.sendDriverApprovedEmail(
                driver.getEmail(),
                driver.getFirstName() + " " + driver.getLastName()
        );

        log.info(
                "Driver Approval Email Sent Successfully. Driver Id : {}, Email : {}",
                driverId,
                driver.getEmail()
        );
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

            log.error("Address update failed for driver id : {}", driverId, e);

            throw new DriverBusinessException(
                    "Failed to update driver address.");
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

        List<DriverOrderHistoryProjection> projections =
                driverOrderRepository.fetchOrderEarningsHistory(driverId);

        List<DriverOrderHistoryDto> response = new ArrayList<>();

        for (DriverOrderHistoryProjection projection : projections) {

            // call customer ms
            DriveOrderDto order = coFeignClients.getOrder(projection.getOrderId());

            log.info("Order Response = {}", order);
            // call fm ms
            log.info("Calling FM with outletId={}", order.getOutletId());

            String outletName = fmFeignClient.fetchOutletName(order.getOutletId());

            log.info("Outlet Name={}", outletName);

            DriverOrderHistoryDto dto = DriverMapper.mapToDriverOrderHistoryDto
                    (projection, order.getOrderStatus(), outletName);

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

        return multiPolygon;
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
    public DriverResponseDto saveOrUpdateProfilePic(
            UploadProfilePicDto uploadProfilePicDto) {

        try {

            // ========================================================
            // 1. VALIDATE IMAGE
            // ========================================================

            validateImage(uploadProfilePicDto.getProfilePicFile());

            Integer userId = uploadProfilePicDto.getUserId();

            String userType = uploadProfilePicDto.getUserType();

            log.info(
                    "[PROFILE PIC] Processing profile picture upload. " +
                            "userId={}, userType={}",
                    userId,
                    userType
            );

//            // ========================================================
//            // 2. UPLOAD IMAGE TO S3
//            // ========================================================
//
//            String s3BucketFilePath =
//                    s3ImageService.uploadFile(
//                            uploadProfilePicDto.getProfilePicFile(),
//                            userId,
//                            userType
//                    );
//
//            log.info(
//                    "[PROFILE PIC] File uploaded to S3 successfully. " +
//                            "userId={}, userType={}, bucketPath={}",
//                    userId,
//                    userType,
//                    s3BucketFilePath
//            );
//
//            // ========================================================
//            // 3. CREATE COMPLETE PROFILE PICTURE URL
//            // ========================================================
//
//            String profilePicUrl =
//                    DConstants.AWS_PROFILE_PIC_STATIC_URL
//                            + s3BucketFilePath;
//
//            log.info(
//                    "[PROFILE PIC] Generated profile picture URL. " +
//                            "userId={}, userType={}, profilePicUrl={}",
//                    userId,
//                    userType,
//                    profilePicUrl
//            );

            // ========================================================
            // 4. DRIVER
            // ========================================================

            if (userType.equalsIgnoreCase(
                    DConstants.TYPE_DRIVER)) {

                log.info(
                        "[PROFILE PIC] Updating DRIVER profile picture. " +
                                "driverId={}",
                        userId
                );

                Driver driver =
                        driverRepository.findById(userId)
                                .orElseThrow(() -> {

                                    log.error(
                                            "Driver not found with id: {}",
                                            userId
                                    );

                                    return new ResourceNotFoundException(
                                            "Driver not found with id: "
                                                    + userId
                                    );
                                });
                // ============================================================
                // Replace old profile picture with new profile picture
                // ============================================================

                String profilePicUrl =
                        replaceProfilePicInS3(
                                driver.getProfilePicUrl(),
                                uploadProfilePicDto.getProfilePicFile(),
                                userId,
                                userType
                        );
                // ----------------------------------------------------
                // Update profile picture URL
                // ----------------------------------------------------

                driver.setProfilePicUrl(profilePicUrl);

                driverRepository.save(driver);

                log.info(
                        "[PROFILE PIC] Driver profile picture updated " +
                                "successfully. driverId={}",
                        userId
                );

                return new DriverResponseDto(
                        DConstants.STATUS_200,
                        "Driver profile picture updated successfully. " +
                                "Profile picture url: " + profilePicUrl
                );
            }

            // ========================================================
            // 5. CUSTOMER
            //
            // Customer table belongs to Customer & Order MS.
            //
            // We only need to update:
            //     1. customerId
            //     2. profilePicUrl
            //
            // We DO NOT fetch the complete customer details.
            // This avoids errors related to customerStatus and
            // other unrelated customer fields.
            // ========================================================

            // ========================================================
            // 5. CUSTOMER
            // ========================================================

            else if (userType.equalsIgnoreCase(
                    DConstants.TYPE_CUSTOMER)) {

                log.info(
                        "[PROFILE PIC] Updating CUSTOMER profile picture. " +
                                "customerId={}",
                        userId
                );

                // ====================================================
                // 1. Fetch ONLY customer profile picture details
                // ====================================================
                //
                // We do NOT fetch the complete customer object.
                // This avoids the previous customerStatus null issue.
                //
                // ====================================================

                ResponseEntity<DriverCustomerProfilePicDto>
                        customerResponse =
                        coFeignClients.getCustomerProfilePic(userId);

                // ====================================================
                // 2. Validate customer response
                // ====================================================

                if (customerResponse == null ||
                        customerResponse.getBody() == null) {

                    log.error(
                            "[PROFILE PIC] Customer not found. customerId={}",
                            userId
                    );

                    throw new ResourceNotFoundException(
                            "Customer not found with id: " + userId
                    );
                }

                // ====================================================
                // 3. Get existing customer profile picture
                // ====================================================

                DriverCustomerProfilePicDto customer =
                        customerResponse.getBody();

                // ====================================================
                // 4. Delete OLD image + Upload NEW image
                // ====================================================
                //
                // replaceProfilePicInS3() does:
                //
                //     1. Delete old S3 image
                //     2. Upload new S3 image
                //     3. Return new S3 URL
                //
                // ====================================================

                String profilePicUrl =
                        replaceProfilePicInS3(
                                customer.getProfilePicUrl(),
                                uploadProfilePicDto.getProfilePicFile(),
                                userId,
                                userType
                        );

                // ====================================================
                // 5. Set new profile picture URL
                // ====================================================

                customer.setCustomerId(userId);

                customer.setProfilePicUrl(
                        profilePicUrl
                );

                log.info(
                        "[PROFILE PIC] Sending customer profile picture " +
                                "update through Feign. customerId={}",
                        userId
                );

                // ====================================================
                // 6. Update Customer in Customer & Order MS
                // ====================================================

                ResponseEntity<DriverResponseDto> response =
                        coFeignClients.updateCustomerProfilePic(
                                customer
                        );

                // ====================================================
                // 7. Validate update response
                // ====================================================

                if (response == null ||
                        response.getBody() == null) {

                    log.error(
                            "[PROFILE PIC] Customer profile picture update " +
                                    "failed. customerId={}",
                            userId
                    );

                    throw new RuntimeException(
                            "Failed to update customer profile picture"
                    );
                }

                // ====================================================
                // 8. Success log
                // ====================================================

                log.info(
                        "[PROFILE PIC] Customer profile picture updated " +
                                "successfully. customerId={}",
                        userId
                );

                // ====================================================
                // 9. Return Driver API response
                // ====================================================

                return new DriverResponseDto(
                        DConstants.STATUS_200,
                        "Customer profile picture updated successfully. " +
                                "Profile picture url: " + profilePicUrl
                );
            }
            // ========================================================
            // 6. MERCHANT
            // ========================================================

            else if (userType.equalsIgnoreCase(
                    DConstants.TYPE_MERCHANT)) {

                log.info(
                        "[PROFILE PIC] Fetching MERCHANT details. " +
                                "merchantId={}",
                        userId
                );

                DriverMerchantDto merchant =
                        fmFeignClient
                                .getMerchantById(userId)
                                .getBody();

                // ============================================================
                // Replace old profile picture with new profile picture
                // ============================================================
                String profilePicUrl =
                        replaceProfilePicInS3(
                                merchant.getProfilePicUrl(),
                                uploadProfilePicDto.getProfilePicFile(),
                                userId,
                                userType
                        );
                // ----------------------------------------------------
                // Validate merchant
                // ----------------------------------------------------

                if (merchant == null ||
                        merchant.getMerchantId() == null) {

                    log.error(
                            "Merchant not found with id: {}",
                            userId
                    );

                    throw new ResourceNotFoundException(
                            "Merchant not found with id: "
                                    + userId
                    );
                }

                merchant.setProfilePicUrl(profilePicUrl);

                // ----------------------------------------------------
                // Set S3 profile picture URL
                // ----------------------------------------------------

                merchant.setProfilePicUrl(profilePicUrl);

                log.info(
                        "[PROFILE PIC] Sending merchant profile picture " +
                                "update through Feign. merchantId={}",
                        userId
                );

                // ----------------------------------------------------
                // Update Merchant in FM MS
                // ----------------------------------------------------

                ResponseEntity<DriverResponseDto> response =
                        fmFeignClient.updateMerchantProfilePic(
                                merchant
                        );

                log.info(
                        "[PROFILE PIC] Merchant profile picture updated " +
                                "successfully. merchantId={}",
                        userId
                );

                // ----------------------------------------------------
                // Return Driver response
                // ----------------------------------------------------

                return new DriverResponseDto(
                        DConstants.STATUS_200,
                        "Merchant profile picture updated successfully. " +
                                "Profile picture url: " + profilePicUrl
                );
            }

            // ========================================================
            // 7. OUTLET
            // ========================================================

            else if (userType.equalsIgnoreCase(
                    DConstants.TYPE_OUTLET)) {

                log.info(
                        "[OUTLET] Processing profile picture for outletId={}",
                        userId
                );

                // ====================================================
                // 1. Fetch outlet details from Food & Mart service
                // ====================================================

                ResponseEntity<DriverFmApiResponse<DriverOutletDto>>
                        responseEntity =
                        fmFeignClient.getOutletById(userId);

                // ====================================================
                // 2. Get FM API response
                // ====================================================

                DriverFmApiResponse<DriverOutletDto> apiResponse =
                        responseEntity.getBody();

                // ====================================================
                // 3. Get outlet details from FM response
                // ====================================================

                if (apiResponse == null ||
                        apiResponse.getData() == null) {

                    throw new ResourceNotFoundException(
                            "Outlet details not found for outletId: "
                                    + userId
                    );
                }
                // ====================================================
                // 4. Extract actual outlet data
                // ====================================================
                DriverOutletDto outlet = apiResponse.getData();

                // ============================================================
                // 5. Replace old profile picture with new profile picture
                //
                // This helper:
                //     1. Deletes old image from S3
                //     2. Uploads new image to S3
                //     3. Returns new profile picture URL
                // ============================================================

                String profilePicUrl =
                        replaceProfilePicInS3(
                                outlet.getOutletPicUrl(),
                                uploadProfilePicDto.getProfilePicFile(),
                                userId,
                                userType
                        );


                // ====================================================
                // 5. Log new profile picture URL
                // ====================================================

                log.info(
                        "[OUTLET] New profile picture URL: {}",
                        profilePicUrl
                );

                // ====================================================
                // 6. Create profile picture DTO
                // ====================================================

                DriverOutletProfilePicDto outletProfilePic = new DriverOutletProfilePicDto();


                outletProfilePic.setOutletId(userId);

                outletProfilePic.setOutletPicUrl(
                        profilePicUrl
                );

                // ====================================================
                // 7. Call FM update outlet profile picture API
                // ====================================================

                ResponseEntity<DriverResponseDto> updateResponse =
                        fmFeignClient.updateOutletProfilePic(
                                outletProfilePic
                        );

                // ====================================================
                // 8. Validate update response
                // ====================================================

                if (updateResponse != null &&
                        updateResponse.getBody() != null) {

                    log.info(
                            "[OUTLET] Profile picture updated successfully. " +
                                    "outletId={}",
                            outlet.getOutletId()
                    );

                    return new DriverResponseDto(
                            DConstants.STATUS_200,
                            "Outlet profile picture updated successfully. " +
                                    "Profile picture url: " + profilePicUrl
                    );
                }

                // ====================================================
                // 9. Fallback response
                // ====================================================

                log.error(
                        "[OUTLET] Empty response received from FM while " +
                                "updating profile picture. outletId={}",
                        outlet.getOutletId()
                );

                return new DriverResponseDto(
                        DConstants.STATUS_200,
                        "Outlet profile picture updated successfully. " +
                                "Profile picture url: " + profilePicUrl
                );
            }

            // ========================================================
            // 10. INVALID USER TYPE
            // ========================================================

            else {

                log.error(
                        "[PROFILE PIC] Invalid user type received: {}",
                        userType
                );

                throw new IllegalArgumentException(
                        "Invalid user type: " + userType
                );
            }

        } catch (IOException e) {

            log.error(
                    "[PROFILE PIC] Error while processing image file",
                    e
            );

            throw new ImageValidationException(
                    "Error processing image file: "
                            + e.getMessage()
            );
        }
    }
    // ================================================================
    // DELETE OLD IMAGE + UPLOAD NEW PROFILE PICTURE
    // ================================================================
    //
    // This helper performs the complete S3 profile picture operation:
    //
    // 1. Deletes the previous profile picture from S3
    // 2. Uploads the new profile picture to S3
    // 3. Generates and returns the new profile picture URL
    //
    // This avoids repeating the same S3 logic for:
    // DRIVER
    // CUSTOMER
    // MERCHANT
    // OUTLET
    //
// ================================================================

    private String replaceProfilePicInS3(
            String oldProfilePicUrl,
            MultipartFile newProfilePic,
            Integer userId,
            String userType) throws IOException {

        // ============================================================
        // 1. Delete previous profile picture
        // ============================================================

        deleteOldProfilePicFromS3(
                oldProfilePicUrl
        );

        // ============================================================
        // 2. Upload new profile picture
        // ============================================================

        String s3BucketFilePath =
                s3ImageService.uploadFile(
                        newProfilePic,
                        userId,
                        userType
                );

        // ============================================================
        // 3. Generate new profile picture URL
        // ============================================================

        String profilePicUrl =
                DConstants.AWS_PROFILE_PIC_STATIC_URL
                        + s3BucketFilePath;

        log.info(
                "[PROFILE PIC] New profile picture uploaded successfully. " +
                        "userId={}, userType={}, url={}",
                userId,
                userType,
                profilePicUrl
        );

        // ============================================================
        // 4. Return new profile picture URL
        // ============================================================

        return profilePicUrl;
    }
//    ==================================================================================
        // ================================================================
        // DELETE OLD PROFILE PICTURE FROM S3
        // ================================================================
        //
        // The database stores the complete S3 URL:
        //
        // https://jippys3bucket.s3.ap-south-2.amazonaws.com/DRIVER/63/driver_old.jpg
        //
        // S3 requires only the object key:
        //
        // DRIVER/63/driver_old.jpg
        //
        // This helper extracts the key and deletes the old image.
        // ================================================================

    private void deleteOldProfilePicFromS3(String oldProfilePicUrl) {

        // ------------------------------------------------------------
        // No previous profile picture
        // ------------------------------------------------------------

        if (oldProfilePicUrl == null ||
                oldProfilePicUrl.trim().isEmpty()) {

            log.info(
                    "[PROFILE PIC] No previous profile picture found. " +
                            "Skipping S3 deletion."
            );

            return;
        }

        try {

            log.info(
                    "[PROFILE PIC] Previous profile picture found. " +
                            "url={}",
                    oldProfilePicUrl
            );

            // --------------------------------------------------------
            // Extract S3 object key from complete URL
            // --------------------------------------------------------

            String s3Key =
                    oldProfilePicUrl.substring(
                            oldProfilePicUrl.indexOf(".com/") + 5
                    );

            log.info(
                    "[PROFILE PIC] Old S3 object key={}",
                    s3Key
            );

            // --------------------------------------------------------
            // Delete old image
            // --------------------------------------------------------

            s3ImageService.deleteFile(s3Key);

            log.info(
                    "[PROFILE PIC] Previous profile picture deleted " +
                            "successfully from S3."
            );

        } catch (Exception e) {

            log.error(
                    "[PROFILE PIC] Failed to delete previous profile " +
                            "picture from S3.",
                    e
            );

            throw new RuntimeException(
                    "Failed to delete previous profile picture",
                    e
            );
        }
    }
//    ===================================================================================
    public void validateImage(MultipartFile file) throws IOException {
        // 1. Check if file is empty
        if (file.isEmpty()) {
            throw new ImageValidationException("File cannot be empty");
        }

        // 2. Validate File Type (MIME Type)
//        String contentType = file.getContentType();
//        if (!isValidType(contentType)) {
//            throw new ImageValidationException("Only PNG, JPEG, and JPG are allowed");
//        }

        // 3. Validate File Size (e.g., Max 5MB)
        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.getSize() > maxSize) {
            throw new ImageValidationException("File size exceeds the 10MB limit");
        }

//        //4. Validate Image Dimensions (e.g., Min 200x200 pixels)
//        BufferedImage image = ImageIO.read(file.getInputStream());
//        if (image == null) {
//            throw new ImageValidationException("Invalid image file");
//        }

//        int width = image.getWidth();
//        int height = image.getHeight();
//
//        if (width < 200 || height < 200) {
//            throw new ImageValidationException("Image must be at least 200x200 pixels");
//        }
    }

    private boolean isValidType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"));
    }

    @Override
    public DriverDto findByEmail(String email) {

        Driver driver = driverRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found for the provided email."));

        DriverDto dto = new DriverDto();

//        dto.setDriverId(driver.getDriverId());
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
    public List<DriverZoneResponseDto> getZones() {
        List<Map<String, Object>> rawRows = zoneRepository.findZones();
        List<DriverZoneResponseDto> responseList = new ArrayList<>();

        for (Map<String, Object> row : rawRows) {
            try {
                Integer id = (Integer) row.get("zone_id");
                String name = (String) row.get("zone_name");
                String geoJsonStr = (String) row.get("boundary_json");

                // Convert stringified GeoJSON from PostGIS into a real JSON Node object
                var boundaryNode = objectMapper.readTree(geoJsonStr);

                responseList.add(new DriverZoneResponseDto(id, name, boundaryNode));
            } catch (Exception e) {
                // Log exception (e.g., JSON parsing failure)
                throw new RuntimeException("Failed to parse spatial boundary data", e);
            }
        }
        return responseList;
    }
//
//    @Override
//    public DriverZoneResponseDto findCommunityById(Integer communityId) {
//
//        Optional<DriverZone> driverZone = zoneRepository.findByZoneIdAndZoneType(communityId, DConstants.COMMUNITY_TYPE);
//        DriverZoneResponseDto driverZoneResponseDto = new DriverZoneResponseDto();
//
//        if (driverZone.isPresent()) {
//            driverZoneResponseDto.setZoneId(driverZone.get().getZoneId());
//            driverZoneResponseDto.setZoneType(driverZone.get().getZoneType());
//            driverZoneResponseDto.setZoneName(driverZone.get().getZoneName());
//
//            return driverZoneResponseDto;
//        }
//        return driverZoneResponseDto;
//    }
//
//    @Override
//    public Integer findCustomerInCommunity(Double latitude, Double longitude) {
//
//        Optional<DriverZone> driverZone = zoneRepository.findCustomerInCommunity(latitude,longitude);
//        if (driverZone.isPresent()) {
//
//            return driverZone.get().getZoneId();
//        }
//        return 0;
//    }
//
//    @Override
//    public Integer checkCustomerAddressWithCommunity(Double latitude, Double longitude,Integer communityId) {
//
//        Optional<DriverZone> driverZone = zoneRepository.checkCustomerAddressWithCommunity(latitude,longitude,communityId);
//        if (driverZone.isPresent()) {
//
//            return driverZone.get().getZoneId();
//        }
//        return 0;
//    }

    //    ================================================================================
    // ================================================================
// READY TO ACCEPT ORDERS TOGGLE
// ================================================================
//
// This method updates the ready_to_accept_orders column
// for the specified driver.
//
// Example:
//
// driverId = 63
// readyToAcceptOrders = true
//
// Database:
//
// driver_id | ready_to_accept_orders
// ----------+-----------------------
// 63        | true
//
// ================================================================
    @Override
    @Transactional
    public DriverResponseDto readyToAcceptIsToggle(
            DriverReadyToAcceptRequestDto requestDto) {

        log.info(
                "[DRIVER TOGGLE] API started. driverId={}, readyToAcceptOrders={}",
                requestDto != null ? requestDto.getDriverId() : null,
                requestDto != null
                        ? requestDto.getReadyToAcceptOrders()
                        : null
        );

        // ============================================================
        // 1. Validate request
        // ============================================================

        if (requestDto == null) {

            throw new IllegalArgumentException(
                    "Driver toggle details are required"
            );
        }

        // ============================================================
        // 2. Validate driver ID
        // ============================================================

        if (requestDto.getDriverId() == null) {

            throw new IllegalArgumentException(
                    "Driver ID is required"
            );
        }

        // ============================================================
        // 3. Validate toggle value
        // ============================================================

        if (requestDto.getReadyToAcceptOrders() == null) {

            throw new IllegalArgumentException(
                    "readyToAcceptOrders value is required"
            );
        }

        // ============================================================
        // 4. Check driver exists
        // ============================================================

        if (!driverRepository.existsById(
                requestDto.getDriverId())) {

            log.error(
                    "[DRIVER TOGGLE] Driver not found. driverId={}",
                    requestDto.getDriverId()
            );

            throw new ResourceNotFoundException(
                    "Driver not found with ID: "
                            + requestDto.getDriverId()
            );
        }

        // ============================================================
        // 5. Update ready_to_accept_orders
        // ============================================================

        int updatedRows =
                driverRepository.updateReadyToAcceptOrders(
                        requestDto.getDriverId(),
                        requestDto.getReadyToAcceptOrders()
                );

        // ============================================================
        // 6. Validate update
        // ============================================================

        if (updatedRows == 0) {

            throw new RuntimeException(
                    "Failed to update driver ready-to-accept-orders status"
            );
        }

        log.info(
                "[DRIVER TOGGLE] Updated successfully. driverId={}, " +
                        "readyToAcceptOrders={}",
                requestDto.getDriverId(),
                requestDto.getReadyToAcceptOrders()
        );

        // ============================================================
        // 7. Prepare success message
        // ============================================================

        String message =
                Boolean.TRUE.equals(
                        requestDto.getReadyToAcceptOrders())
                        ? "Driver is ready to accept orders"
                        : "Driver is not ready to accept orders";

        // ============================================================
        // 8. Return status code + message
        // ============================================================

        return new DriverResponseDto(
                "200",
                message
        );
    }
}