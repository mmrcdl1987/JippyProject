    package com.jippy.driver.controller;


    import com.jippy.driver.constants.DConstants;
    import com.jippy.driver.dto.*;
    import com.jippy.driver.service.DriverService;
    import com.jippy.driver.serviceImpl.DriverLocationService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.Positive;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.multipart.MultipartFile;

    import java.time.LocalDate;
    import java.util.List;

    @RestController
    @RequestMapping("/api/driver")
    @RequiredArgsConstructor
    @Slf4j
    @Validated
    @Tag(name = "Driver API", description = "Driver and KYC operations")
    public class DriverController {

        private final DriverService driverService;
        private final DriverLocationService driverLocationService;

        @PostMapping(path = "/postDriverDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(
                summary = "Create Driver",
                description = "Creates a new Driver along with Driver KYC, Address, User Account, "
                        + "Wallet and Approval Request. "
                        + "The Driver is saved in the Driver Microservice, while the Address, "
                        + "User Account and Approval Request are created in the Food & Mart "
                        + "Microservice through Feign Client integration."
                        + "Supports document uploads for KYC verification.")
        @ApiResponse(responseCode = "200", description = "Driver created successfully.")
        @ApiResponse(responseCode = "400", description = "Invalid Driver request.")
        @ApiResponse(responseCode = "404", description = "Referenced resource not found.")
        @ApiResponse(responseCode = "500", description = "Internal Server Error.")
        public ResponseEntity<DriverDto> postDriverDetails(
                @Valid @ModelAttribute DriverDto dto,
                @RequestPart(value = "aadharDocument", required = false) MultipartFile aadharDocument,
                @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
                @RequestPart(value = "drivingLicenseDocument", required = false) MultipartFile drivingLicenseDocument,
                @RequestPart(value = "rcCopyDocument", required = false) MultipartFile rcCopyDocument) {

            log.info("POST API called that created driver:");

            return ResponseEntity.ok(driverService.postDriverDetails(
                    dto, aadharDocument, panDocument, drivingLicenseDocument, rcCopyDocument));
        }

        //    get driver details ,driver kyc from this this(Co Microservice) and address Details from (FM microservices)
        @GetMapping("/getDriverDetails")
        @Operation(summary = "Get Driver", description = "Fetch driver by ID")
        public ResponseEntity<DriverDto> getDriverDetails(

                @Positive(message = "Driver ID must be greater than zero")
                @RequestParam Integer driverId) {

            log.info("GET API called with id to get all details of driver : {}", driverId);

            return ResponseEntity.ok(driverService.getDriverDetails(driverId));
        }

        @GetMapping("/getAllDrivers")
        @Operation(
                summary = "Get All Drivers",
                description = "Fetches all drivers along with their address details"
        )
        @ApiResponse(
                responseCode = "200",
                description = "Drivers fetched successfully"
        )
        public ResponseEntity<List<DriverDto>> getAllDrivers() {

            log.info("GET_ALL_DRIVERS_API_CALLED");

            return ResponseEntity.ok(
                    driverService.getAllDrivers()
            );
        }

        //    update driver details ,driver kyc from this this(Co Microservice)
    //    and address Details from (FM microservices)
        @PutMapping(path = "/updateDriverDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Update Driver Details", description = "Updates editable driver and address fields. Supports document uploads for KYC verification.")
        public ResponseEntity<DriverDto> updateDriverDetails(

                @RequestParam Integer driverId,
                @ModelAttribute DriverDto dto,
                @RequestPart(value = "aadharDocument", required = false) MultipartFile aadharDocument,
                @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
                @RequestPart(value = "drivingLicenseDocument", required = false) MultipartFile drivingLicenseDocument,
                @RequestPart(value = "rcCopyDocument", required = false) MultipartFile rcCopyDocument) {

            log.info("Updating driver with id: {}", driverId);

            return ResponseEntity.ok(driverService.updateDriverDetails(
                    driverId, dto, aadharDocument, panDocument, drivingLicenseDocument, rcCopyDocument));
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

                (        @Positive(message = "Driver ID must be greater than zero.")
                         @RequestParam Integer driverId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

            log.info("date format must be [YYYY-MM-dd] for date: {}", date);
            log.info("Fetch earnings API called for driver id: {}", driverId);

            return ResponseEntity.ok(driverService.fetchEarnings(driverId, date));
        }

        //    for api fetchOrderEarningsHistory to just fetch
    //    outlet name based on outlet id which is mapped to driver id to
    //    use CoDriverController microservice
        @GetMapping("/fetchOrderEarningsHistory")
        @Operation(summary = "Fetch Order Earnings History",
                description = "Fetch complete order earnings history of driver")
        public ResponseEntity<List<DriverOrderHistoryDto>> fetchOrderEarningsHistory(
                @Positive(message = "Driver ID must be greater than zero.")
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
                @Positive(message = "Driver ID must be greater than zero.")
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
            DriverResponseDto response = driverService.saveOrUpdateProfilePic(uploadProfilePicDto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/readyToAcceptIsToggle")
        @Operation(summary = "Toggle ready-to-accept orders", description = "Enables or disables a driver's availability to accept orders")
        public ResponseEntity<DriverResponseDto> readyToAcceptIsToggle(@Valid @RequestBody DriverReadyToAcceptRequestDto requestDto) {
            log.info("Ready to accept toggle API called for driver id: {}", requestDto.getDriverId());
            return ResponseEntity.ok(driverService.readyToAcceptIsToggle(requestDto));
        }

//        used for forget password api in Fm
        @GetMapping("/findByEmail")
        @Operation(summary = "Find Driver By Email")
        public ResponseEntity<DriverDto> findByEmail(
                @Email(message = "Please enter a valid email address.")
                @RequestParam String email) {

            log.info("Finding driver by email : {}", email);

            return ResponseEntity.ok(driverService.findByEmail(email));
        }
        //        --------------------------------------------------------------------------------------

        /**
         * ===========================================================
         * Get Driver Details by Driver Id
         * ===========================================================
         *
         * This API is used by the Food & Mart (FM) microservice
         * during the Level-1 Approval process.
         *
         * It fetches complete Driver information including:
         * 1. Driver Details
         * 2. Driver KYC Details
         * 3. Driver Address Details
         *
         * @param driverId Driver Id
         * @return Driver Approval Response
         */
        @GetMapping("/getDriverById/{driverId}")
        public ResponseEntity<FmDriverApprovalResponseDTO> getDriverById(
                @PathVariable Integer driverId) {

            return ResponseEntity.ok(driverService.getDriverById(driverId));
        }

        @GetMapping("/getZones")
        public ResponseEntity<List<DriverZoneResponseDto>> getZones() {

            List<DriverZoneResponseDto> zones = driverService.getZones();

            return ResponseEntity.status(HttpStatus.OK).body(zones);
        }

//        @GetMapping("/findCommunityById")
//        public ResponseEntity<DriverZoneResponseDto> findCommunityById(@RequestParam(value = "communityId") Integer communityId) {
//
//            DriverZoneResponseDto zones = driverService.findCommunityById(communityId);
//
//            return ResponseEntity.status(HttpStatus.OK).body(zones);
//        }

//        @GetMapping("/findCustomerInCommunity")
//        public ResponseEntity<Integer> findCustomerInCommunity(@RequestParam Double latitude,
//                @RequestParam Double longitude) {
//
//            Integer status = driverService.findCustomerInCommunity(latitude,longitude);
//
//            return ResponseEntity.status(HttpStatus.OK).body(status);
//        }

//        @GetMapping("/checkCustomerAddressWithCommunity")
//        public ResponseEntity<Integer> checkCustomerAddressWithCommunity(@RequestParam Double latitude,
//                @RequestParam Double longitude,@RequestParam Integer communityId) {
//
//            Integer status = driverService.checkCustomerAddressWithCommunity(latitude,longitude,communityId);
//
//            return ResponseEntity.status(HttpStatus.OK).body(status);
//        }
//        @GetMapping("/checkCustomerAddressWithCommunity")
//        public ResponseEntity<Integer> checkCustomerAddressWithCommunity(@RequestParam Double latitude,
//                @RequestParam Double longitude,@RequestParam Integer communityId) {
//
//            Integer status = driverService.checkCustomerAddressWithCommunity(latitude,longitude,communityId);
//
//            return ResponseEntity.status(HttpStatus.OK).body(status);
//        }
        /**
         * Approves the Driver.
         *
         * @param driverId Driver Id
         */
        @PutMapping("/approve/{driverId}")
        public ResponseEntity<Void> approveDriver(
                @PathVariable Integer driverId) {

            log.info("Received request to approve Driver. Driver Id : {}", driverId);

            driverService.approveDriver(driverId);

            log.info("Driver approved successfully. Driver Id : {}", driverId);

            return ResponseEntity.ok().build();
        }


    }