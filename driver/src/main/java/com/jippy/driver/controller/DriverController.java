    package com.jippy.driver.controller;


    import com.jippy.driver.constants.DConstants;
    import com.jippy.driver.dto.*;
    import com.jippy.driver.service.DriverService;
    import com.jippy.driver.serviceImpl.DriverLocationService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.util.List;

    @RestController
    @RequestMapping("/api/driver")
    @RequiredArgsConstructor
    @Slf4j
    @Tag(name = "Driver API", description = "Driver and KYC operations")
    public class DriverController {

        private final DriverService driverService;
        private final DriverLocationService driverLocationService;

        //    post driver details ,driver kyc from this this(Co Microservice) and address Details from (FM microservices)
        @PostMapping("/postDriverDetails")
        @Operation(summary = "Create Driver", description = "Creates driver, KYC, and address")
        public ResponseEntity<DriverDto> postDriverDetails
        ( @Valid @RequestBody DriverDto dto) {

            log.info("POST API called that created driver:");

            return ResponseEntity.ok(driverService.postDriverDetails(dto));
        }

        //    get driver details ,driver kyc from this this(Co Microservice) and address Details from (FM microservices)
        @GetMapping("/getDriverDetails")
        @Operation(summary = "Get Driver", description = "Fetch driver by ID")
        public ResponseEntity<DriverDto> getDriverDetails(

                @RequestParam Integer driverId) {

            log.info("GET API called with id to get all details of driver : {}", driverId);

            return ResponseEntity.ok(driverService.getDriverDetails(driverId));
        }

        //    update driver details ,driver kyc from this this(Co Microservice)
    //    and address Details from (FM microservices)
        @PutMapping("/updateDriverDetails")
        @Operation(summary = "Update Driver Details", description = "Updates editable driver and address fields")
        public ResponseEntity<DriverDto> updateDriverDetails(

                @RequestParam Integer driverId,
                @RequestBody DriverDto dto) {

            log.info("Updating driver with id: {}", driverId);

            return ResponseEntity.ok(driverService.updateDriverDetails(driverId, dto));
        }

        @PostMapping("/createZones")
        @Operation(summary = "Create Zones", description = "Create Zones")
        public ResponseEntity<DriverResponseDto> createZones(@Valid @RequestBody DriverZoneDto zoneDto) {

            log.info("POST API called for created zones:");
            String message = driverService.createZones(zoneDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(new DriverResponseDto(DConstants.STATUS_201, message));
        }

        @GetMapping("/fetchEarnings")
        @Operation(summary = "Fetch Driver Earnings", description = "Fetch total earnings and orders count for a driver on a particular date")
        public ResponseEntity<DriverEarningsDto> fetchEarnings
                (@RequestParam Integer driverId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

            log.info("date format must be [YYYY-MM-dd] for date: {}", date);
            log.info("Fetch earnings API called for driver id: {}", driverId);

            return ResponseEntity.ok(driverService.fetchEarnings(driverId, date));
        }

        //    for api fetchOrderEarningsHistory to just fetch
    //    outlet name based on outlet id which is mapped to driver id to
    //    use CoDriverController microservice
        @GetMapping("/fetchOrderEarningsHistory")
        @Operation(summary = "Fetch Order Earnings History", description = "Fetch complete order earnings history of driver")
        public ResponseEntity<List<DriverOrderHistoryDto>> fetchOrderEarningsHistory(

                @RequestParam Integer driverId){

            log.info("Fetch order earnings history API called for driver id: {}", driverId);

            return ResponseEntity.ok(driverService.fetchOrderEarningsHistory(driverId));
        }

    //    to fetch total earnings details of driver like total pick up charges,
    //    total delivery charges, total tips, total surge fee and total earnings
    //    which is sum of all these and also count of rejected orders for that driver
        @GetMapping("/fetchTotalEarnings")
        @Operation(summary = "Fetch Total Earnings", description = "Fetch total earnings details of driver")
        public ResponseEntity<DriverTotalEarningsDto> fetchTotalEarnings(

                @RequestParam Integer driverId) {

            log.info("Fetch total earnings API called for driver id: {}", driverId);

            return ResponseEntity.ok(driverService.fetchTotalEarnings(driverId));
        }

        @PostMapping("/updatedDriverDeliveryLocation")
        @Operation(summary = "Update Driver Location", description = "Call this API to update driver location when driver is on the way to deliver the order for every 5sec from driver Application")
        public ResponseEntity<DriverResponseDto> updatedDriverDeliveryLocation(@Valid @RequestBody UpdateDriverLocationDto updateDriverLocationDto) {

            log.info("Update driver location API called for driver id: {}", updateDriverLocationDto.getDriverId());
           String message = driverLocationService.updateLiveLocation(updateDriverLocationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(new DriverResponseDto(DConstants.STATUS_200, message));
        }

        @PostMapping("/driverDeliveredOrder")
        @Operation(summary = "Driver Delivered Order", description = "After successful delivery of order driver will call this API to update the order status to delivered and also update the driver earnings details in driver_orders table")
        public ResponseEntity<DriverResponseDto> driverDeliveredOrder(@Valid @RequestBody DriverOrderDto driverOrderDto) {

            log.info("Driver delivered order API called for driver id: {}", driverOrderDto.getDriverId());
            String message = driverService.driverDeliveredOrder(driverOrderDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(new DriverResponseDto(DConstants.STATUS_200, message));
        }

        @PostMapping(path = "/saveOrUpdateProfilePic",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload Profile Pic", description = "Merchant,Driver,Customer can upload their profile pic using this API and the file will be stored in AWS S3 bucket and the URL of the file will be stored in database and also return the URL of the file in response")
        public ResponseEntity<DriverResponseDto> saveOrUpdateProfilePic(@ModelAttribute UploadProfilePicDto uploadProfilePicDto) {

            log.info("Upload Profile Pic API called for user id: {}", uploadProfilePicDto.getUserId());
            String message = driverService.saveOrUpdateProfilePic(uploadProfilePicDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(new DriverResponseDto(DConstants.STATUS_200, message));
        }

//        used for forget password api in Fm
        @GetMapping("/findByEmail")
        @Operation(summary = "Find Driver By Email")
        public ResponseEntity<DriverDto> findByEmail(@RequestParam String email) {

            log.info("Finding driver by email : {}", email);

            return ResponseEntity.ok(driverService.findByEmail(email));
        }

    }