package com.jippy.driver.serviceImpl;


import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.*;
import com.jippy.driver.entity.*;
import com.jippy.driver.exception.CoZoneException;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.feignClients.FMFeignClient;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.projection.DriverEarningsProjection;
import com.jippy.driver.projection.DriverOrderHistoryProjection;
import com.jippy.driver.projection.DriverTotalEarningsProjection;
import com.jippy.driver.repositary.*;
import com.jippy.driver.service.DriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final DriverWalletRepository driverWalletRepository;
    private final FMFeignClient fmFeignClient;
    private final DriverLocationService driverLocationService;

    @Override
    @Transactional
    public DriverDto postDriverDetails(DriverDto dto) {

        log.info("Creating driver for phone: {}", dto.getPhoneNumber());

        // Convert DTO → Entity
        Driver driver = DriverMapper.mapToDriverEntity(dto);

        // Save driver
        Driver savedDriver = driverRepository.save(driver);

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

        log.info("Fetching earnings for driver id: {} and date: {}", driverId, date);

        // Validate driver exists
        driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        // Fetch orders count for the driver on the given date
//        Long ordersCount = ordersRepository.countOrdersByDriverAndDate(driverId, date);

        // Fetch total earnings for the driver on the given date
        // Fetch earnings + count together to avoid multiple DB calls

        //DriverEarningsProjection projectionOfTotalEarningsAndCountOfOrders = ordersRepository.fetchDriverEarnings(driverId, date);
        DriverEarningsDto projectionOfTotalEarningsAndCountOfOrders = coFeignClients.fetchDriverEarnings(driverId, date);
        // Prepare response DTO
        DriverEarningsDto driverEarningsDto = new DriverEarningsDto();

        driverEarningsDto.setDriverId(driverId);
        driverEarningsDto.setCurrentDate(date);


//        driverEarningsDto.setOrdersCountToday(projectionOfTotalEarningsAndCountOfOrders.getOrdersCount());
//        driverEarningsDto.setTotalEarningsToday(projectionOfTotalEarningsAndCountOfOrders.getTotalEarnings());
        driverEarningsDto.setOrdersCountToday(projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday());
        driverEarningsDto.setTotalEarningsToday(projectionOfTotalEarningsAndCountOfOrders.getTotalEarningsToday());

        //
        // new requirement : Calculate Incentive Bonus (Range-Based Logic)

        BigDecimal bonus = BigDecimal.ZERO;

        // Fetch all slabs sorted by orders_count ascending
        List<DriverIncentiveSettings> slabs = driverIncentivesettingsRepository.findAllSlabs();

//        Integer orders = projectionOfTotalEarningsAndCountOfOrders.getOrdersCount() != null
//                ? projectionOfTotalEarningsAndCountOfOrders.getOrdersCount().intValue()
//                : 0;

        Integer orders = projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday() != null ? projectionOfTotalEarningsAndCountOfOrders.getOrdersCountToday().intValue() : 0;
        log.info("Total orders completed by driver {}: {}", driverId, orders);

        //Instead of loop → use mapper
        // Find correct slab → assign its final value (no addition of values)
        bonus = DriverMapper.calculateIncentiveBonus(slabs, orders);

        log.info("Calculated bonus from mapper: {}", bonus);

        // Step 5: Set bonus into DTO
        driverEarningsDto.setDriverIncentiveBonus(bonus);

        log.info("Final incentive bonus for driver {}: {}", driverId, bonus);

        log.info("Successfully fetched earnings for driverId: {}", driverId);

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
    public List<DriverOrderHistoryDto> fetchOrderEarningsHistory(
            Integer driverId
    ) {

        log.info(
                "Fetching order earnings history for driver id: {}",
                driverId);

        driverRepository.findById(driverId)
                .orElseThrow(() -> {

                    log.error(
                            "Driver not found with id: {}",
                            driverId);

                    return new ResourceNotFoundException(
                            "Driver not found with id: " + driverId);
                });

        List<DriverOrderHistoryProjection> projections =
                driverOrderRepository
                        .fetchOrderEarningsHistory(driverId);

        List<DriverOrderHistoryDto> response =
                new ArrayList<>();

        for (DriverOrderHistoryProjection projection : projections) {

            // call customer ms
            DriveOrderDto order =
                    coFeignClients.getOrder(
                            String.valueOf(
                                    projection.getOrderId()));

            // call fm ms
            String outletName =
                    fmFeignClient.fetchOutletName(
                            order.getOutletId());

            DriverOrderHistoryDto dto =
                    DriverMapper.mapToDriverOrderHistoryDto(
                            projection,
                            order.getOrderStatus(),
                            outletName);

            response.add(dto);
        }

        log.info(
                "Successfully fetched order earnings history for driver id: {}",
                driverId);

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
        Long rejectedOrders =
                coFeignClients
                        .fetchRejectedOrdersCount(driverId);
        // Convert to DTO using mapper
        DriverTotalEarningsDto response = DriverMapper.mapToTotalEarningsDto(driverId, projection, rejectedOrders);

        log.info("Successfully fetched total earnings for driver id: {}", driverId);

        return response;
    }

    @Transactional
    @Override
    public String createZones(DriverZoneDto zoneDto) {

        Optional<DriverZone> existingZone = zoneRepository.findByZoneName(zoneDto.getZoneName());
        Polygon polygon = convertToJtsPolygon(zoneDto.getBoundary());
        if (existingZone.isPresent()) {
            if (zoneRepository.existsBySpatialBoundary(polygon)) {
                throw new CoZoneException("A boundary with this exact shape already exists!");
            } else {
                log.info("Updating existing zone with id: {}", existingZone.get().getZoneId());
                DriverZone zoneToUpdate = existingZone.get();
                zoneToUpdate.setBoundary(polygon);
                zoneToUpdate.setUpdatedAt(LocalDateTime.now());
                zoneToUpdate.setUpdatedBy(zoneDto.getCreatedBy());
                zoneRepository.save(zoneToUpdate);
                return "Zone:" + zoneToUpdate.getZoneName() + " updated successfully!";
            }
        }
        DriverZone zone = DriverMapper.mapToZoneEntity(zoneDto, polygon);
        DriverZone savedZone = zoneRepository.save(zone);
        log.info("New zone is created with id: {}", savedZone.getZoneId());

        return "Zone:" + zone.getZoneName() + " created successfully!";
    }

    private Polygon convertToJtsPolygon(List<DriverZoneDto.CoordinateDTO> boundary) {
        GeometryFactory factory = new GeometryFactory();

        Coordinate[] coords = boundary.stream().map(p -> new Coordinate(p.getLongitude(), p.getLatitude())).toArray(Coordinate[]::new);

        // JTS requires the linear ring to be closed (first == last)
        return factory.createPolygon(coords);
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
}