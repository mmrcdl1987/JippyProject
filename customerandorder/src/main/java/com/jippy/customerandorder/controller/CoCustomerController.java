package com.jippy.customerandorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.iservice.CoCustomerDeliveryService;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CoCustomerController {

    private final ICoCustomerService customerService;
    private final CoCustomerDeliveryAddressRepository repository;
    private final CoCustomerDeliveryService customerDeliveryAddressService;
    private final ObjectMapper objectMapper;

    // CREATE CUSTOMER
    @PostMapping
    public CoCustomer createCustomer(@RequestBody CoCustomerRequestDto dto) {

        log.info("Customer create request received: {}", dto);

        // Validate referral code if provided
        if (dto.getReferralCodeUsed() != null && !dto.getReferralCodeUsed().isBlank()) {
            log.info("Referral code validation: {}", dto.getReferralCodeUsed());
        }
        return customerService.createCustomer(dto);
    }

    // CONVERT POINTS
    @PostMapping("/convert-points/{customerId}")
    public CoWalletResponseDto convertPoints(@PathVariable Integer customerId) {

        log.info("Convert points request received for customerId : {}", customerId);

        return customerService.convertPoints(customerId);
    }

    @PostMapping("/daily-streak/{customerId}")
    public CoCustomerStreakResponseDto updateDailyStreak(

            @PathVariable Integer customerId,

            @RequestParam(required = false) LocalDate date) {

        log.info("Daily streak request received");

        return customerService.updateDailyStreak(customerId, date);
    }

    @PostMapping("/wallet/transfer")
    public CoWalletTransferResponseDto transferWalletPoints(@RequestBody CoWalletTransferRequestDto requestDto) {

        log.info("Wallet transfer request received");

        return customerService.transferWalletPoints(requestDto);
    }

    // GET CUSTOMER

    @GetMapping("/{customerId}")
    public ResponseEntity<CoCustomerResponseDto> getCustomer(@PathVariable Integer customerId) {

        log.info("GET_CUSTOMER_API_START | customerId={}", customerId);

        CoCustomerResponseDto customer = customerService.getCustomer(customerId);

        log.info("GET_CUSTOMER_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(customer);
    }

    // UPDATE CUSTOMER
    @PutMapping("/{customerId}")
    public ResponseEntity<CoResponseDto> updateCustomer(@PathVariable Integer customerId, @Valid @RequestBody CoCustomerRequestDto requestDto) {

        log.info("UPDATE_CUSTOMER_API_START | customerId={} | email={} | phone={}", customerId, requestDto.getEmail(), requestDto.getPhoneNumber());

        customerService.updateCustomer(customerId, requestDto);

        log.info("UPDATE_CUSTOMER_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(new CoResponseDto(COConstants.STATUS_200, COConstants.MSG_SUCCESS));
    }

    // GET CUSTOMER LOCATION
    @GetMapping("/address/location")
    public CoCustomerLocationDto getCustomerLocation(@RequestParam Integer customerAddressId) {

        log.info("GET_CUSTOMER_LOCATION_API_START | customerAddressId={}", customerAddressId);

        CustomerLocationProjection projection = repository.getCustomerLocation(customerAddressId);

        CoCustomerLocationDto dto = new CoCustomerLocationDto();

        dto.setLatitude(projection.getLatitude());
        dto.setLongitude(projection.getLongitude());

        log.info("GET_CUSTOMER_LOCATION_API_SUCCESS | customerAddressId={}", customerAddressId);

        return dto;
    }


    // Update Customer Profile Pic
    @PutMapping(value = "/updateCustomerProfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CoResponseDto> updateCustomerProfile(@RequestPart("customerData") String customerDataJson, @RequestPart(value = "profilePic", required = false) MultipartFile profilePic) throws Exception {

        CoCustomerRequestDto requestDto = objectMapper.readValue(customerDataJson, CoCustomerRequestDto.class);

        log.info("UPDATE_CUSTOMER_PROFILE_API_START | customerId={}", requestDto.getCustomerId());

        String result = customerService.updateCustomerProfile(requestDto, profilePic);

        log.info("UPDATE_CUSTOMER_PROFILE_API_SUCCESS | customerId={}", requestDto.getCustomerId());

        return ResponseEntity.ok(new CoResponseDto(COConstants.STATUS_200, result));
    }

    //    ----------------------------------------------------------------------------------------------
    //    to post customer delivery address with latitude and longitude
    @PostMapping("/saveCustomerDeliveryAddress")
    @Operation(summary = "Create Customer Delivery Address", description = "Creates and stores a customer delivery address using the provided customer details and geographic coordinates. " + "The latitude and longitude values are converted into a geographic Point location and persisted in the customer_delivery_addresses table. " + "Mandatory fields: customerId, latitude, longitude, doorNo, buildingName, laneNo, area, city and createdBy. " + "Returns the saved customer delivery address details including the generated customerAddressId.")
    public ResponseEntity<CoCustomerDeliveryAddressResponseDto> createCustomerDeliveryAddress(@Valid @RequestBody CoCustomerDeliveryAddressRequestDto requestDto) {

        log.info("CREATE_CUSTOMER_ADDRESS_API_START | customerId={}", requestDto.getCustomerId());

        CoCustomerDeliveryAddressResponseDto responseDto = customerDeliveryAddressService.createCustomerDeliveryAddress(requestDto);

        log.info("CREATE_CUSTOMER_ADDRESS_API_SUCCESS | customerAddressId={}", responseDto.getCustomerAddressId());

        return ResponseEntity.ok(responseDto);

    }

    //    get list of all the delivery addresses of a customer based on the customer id
    @GetMapping("/getCustomerDeliveryAddresses")
    @Operation(summary = "Get Customer Delivery Addresses", description = "Fetches all delivery addresses associated with the given customerId. " + "ex input is customerId=123. The API retrieves the list of delivery addresses for" + " the specified customer from the database and returns them as a list of CoCustomerDeliveryAddressResponseDto objects. " + "Each object in the response contains details about a delivery address, including customerAddressId, customerId," + " latitude, longitude, doorNo, buildingName, laneNo, area, and city. If no addresses are found for the provided customerId, an empty list is returned.")
    public ResponseEntity<List<CoCustomerDeliveryAddressResponseDto>> getCustomerDeliveryAddresses(@Positive(message = "Customer Id must be greater than zero") @RequestParam Integer customerId) {

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_API_START | customerId={}", customerId);
        log.info("Fetching delivery addresses for customerId={}", customerId);

        List<CoCustomerDeliveryAddressResponseDto> responseDto = customerDeliveryAddressService.getCustomerDeliveryAddresses(customerId);

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_API_SUCCESS | customerId={} | addressCount={}", customerId, responseDto.size());

        return ResponseEntity.ok(responseDto);
    }

    //    to delete a delivery address based on the customer_address_id
    @DeleteMapping("/deleteCustomerDeliveryAddress")
    @Operation(summary = "Delete Customer Delivery Address", description = "Deletes a customer delivery address using the provided customerAddressId.")
    public ResponseEntity<CoResponseDto> deleteCustomerDeliveryAddress(@RequestParam Integer customerAddressId) {

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_API_START | customerAddressId={}", customerAddressId);
        log.info("Attempting to delete customer delivery address with customerAddressId={}", customerAddressId);

        customerDeliveryAddressService.deleteCustomerDeliveryAddress(customerAddressId);

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_API_SUCCESS | customerAddressId={}", customerAddressId);

        CoResponseDto customerDeliveryAddressDeletedSuccessfullyDto = new CoResponseDto(COConstants.STATUS_200, "Customer delivery address deleted successfully");

        return ResponseEntity.ok(customerDeliveryAddressDeletedSuccessfullyDto);

    }

    @GetMapping
    public ResponseEntity<List<CoCustomerListDto>> getAllCustomers() {

        log.info("GET_ALL_CUSTOMERS_API_START");

        List<CoCustomerListDto> customers = customerService.getAllCustomers();

        log.info("GET_ALL_CUSTOMERS_API_SUCCESS | count={}", customers.size());

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/wallet/{customerId}")
    public ResponseEntity<CoCustomerWalletResponseDto> getCustomerWallet(@PathVariable Integer customerId) {

        log.info("GET_CUSTOMER_WALLET_API_START | customerId={}", customerId);

        CoCustomerWalletResponseDto response = customerService.getCustomerWallet(customerId);

        log.info("GET_CUSTOMER_WALLET_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/history/{customerId}")
    public ResponseEntity<List<CoWalletTransactionHistoryDto>> getWalletTransactionHistory(@PathVariable Integer customerId) {

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_START | customerId={}", customerId);

        List<CoWalletTransactionHistoryDto> response = customerService.getWalletTransactionHistory(customerId);

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_SUCCESS | customerId={}", customerId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile-incomplete")
    public ResponseEntity<List<CoProfileIncompleteCustomer>> getProfileIncompleteCustomers() {

        log.info("Received request to fetch customers with incomplete profiles.");

        List<CoProfileIncompleteCustomer> customers = customerService.getProfileIncompleteCustomers();

        return ResponseEntity.ok(customers);
    }

    // ================================================================
    // UPDATE CUSTOMER PROFILE PICTURE
    // ================================================================

    @PutMapping("/updateCustomerProfilePic")
    public ResponseEntity<CoResponseDto> updateCustomerProfilePic(@RequestBody CustomerProfilePicDto customerDto) {

        log.info("[CUSTOMER] Updating profile picture. customerId={}", customerDto.getCustomerId());

        String message = customerService.updateCustomerProfilePic(customerDto);

        return ResponseEntity.ok(new CoResponseDto("200", message));
    }
//    ====================================================================================
//    ====================================================================================

    /**
     * Fetch complete order flow counts based on order status.
     */
    @GetMapping("/getCompleteOrdersFlowCounts")
    @Operation(summary = "Get complete orders flow counts", description = "Fetches the total number of orders and counts of orders " + "in ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED and " + "ORDER_COMPLETED statuses.")
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Complete orders flow counts fetched successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CoCompleteOrdersFlowCountsDto.class))),

            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ResponseEntity<CoCompleteOrdersFlowCountsDto> getCompleteOrdersFlowCounts() {

        log.info("Received request to fetch complete orders flow counts");

        CoCompleteOrdersFlowCountsDto response = customerService.getCompleteOrdersFlowCounts();

        log.info("Returning complete orders flow counts successfully");

        return ResponseEntity.ok(response);
    }
//    ========================================================================================
//    ========================================================================================

    /**
     * Fetches complete order details based on the provided order status.
     * <p>
     * The API returns:
     * - Order ID
     * - Outlet ID
     * - Driver ID
     * - Order Status
     * <p>
     * The result is filtered using the order_status column
     * from the orders table.
     */
    @GetMapping("/getCompleteOrdersDetailsByOrderStatus")
    @Operation(
            summary = "Get complete order details by order status",
            description = "Fetches complete order details for all orders matching the supplied order status with pagination support. " +
                    "Supported values: ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_COMPLETED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order details fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing order status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<CoOrderDetailsByOrderStatusDto>>
                                        getCompleteOrdersDetailsByOrderStatus(
            @Parameter(
                    name = "orderStatus",
                    description = "Order status filter. Supported values: ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_COMPLETED.",
                    example = "ORDER_SHIPPED",
                    required = true
            )
            @RequestParam String orderStatus,

            @Parameter(
                    name = "page",
                    description = "Page number (starts from 0)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    name = "size",
                    description = "Number of records per page",
                    example = "5"
            )
            @RequestParam(defaultValue = "5") int size) {

        log.info(
                "Received request to fetch complete order details. orderStatus={}, page={}, size={}",
                orderStatus,
                page,
                size
        );

        Pageable pageable = PageRequest.of(page, size);

        Page<CoOrderDetailsByOrderStatusDto> response =
                customerService.getCompleteOrdersDetailsByOrderStatus(
                        orderStatus,
                        pageable
                );

        log.info(
                "Successfully fetched {} orders for orderStatus={}, page={}, size={}",
                response.getNumberOfElements(),
                orderStatus,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }
    //    ========================================================================================
//    ========================================================================================
    @GetMapping("/getOrderCompleteDetails")
    @Operation(summary = "Get complete order details", description = """
            Fetches complete order information using [Input] ---> "order ID".
            
            The response includes:
            - Order ID
            - Created by
            - Order type
            - Order status
            - Payment mode
            - Customer details
            - Customer building name
            - Outlet details
            - Outlet building number
            - Order items
            - Order price breakup
            
            If the order status is ORDER_REJECTED,
            refund details are also fetched from the
            Division microservice.
            """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Complete order details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid order ID"), @ApiResponse(responseCode = "404", description = "Order not found"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<CoOrderCompleteDetailsResponseDto> getOrderCompleteDetails(@RequestParam String orderId) {

        log.info("GET /getOrderCompleteDetails request received. " + "orderId={}", orderId);

        CoOrderCompleteDetailsResponseDto response = customerService.getOrderCompleteDetails(orderId);

        log.info("GET /getOrderCompleteDetails completed successfully. " + "orderId={}", orderId);

        return ResponseEntity.ok(response);
    }

//    =====================================================================================
//    =====================================================================================

    /**
     * Fetches order flow counts for either a merchant, outlet, or driver.
     *
     * <p>
     * Exactly one of merchantId, outletId or driverId must be provided.
     *
     * <p>
     * If merchantId is provided:
     * CO fetches all outlets belonging to that merchant
     * from Food & Mart and calculates order counts for those outlets.
     *
     * <p>
     * If outletId is provided:
     * CO directly calculates order counts for that outlet.
     *
     * <p>
     * If driverId is provided:
     * CO directly calculates order counts for that driver
     * from the orders table.
     */
    @GetMapping("/getOrderFlowCountForMerchantOrOutletOrDriver")
    @Operation(summary = "Get order flow counts for merchant, outlet or driver", description = """
            Fetches total, completed and rejected order counts.
            
            Exactly one of [ merchantId ], [ outletId ] OR [ driverId ]
            must be provided.
            
            If merchantId is provided, all outlets belonging to
            that merchant are fetched from Food & Mart and the
            order counts are calculated across those outlets.
            
            If outletId is provided, the order counts are calculated
            directly for that outlet from CO.
            
            If driverId is provided, the order counts are calculated
            directly for that driver from the CO orders table.
            """)
    @ApiResponses(value = {

            @ApiResponse(responseCode = "200", description = "Order flow counts fetched successfully"),

            @ApiResponse(responseCode = "400", description = "Exactly one of merchantId, outletId or driverId must be provided"),

            @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<CoOrderFlowCountForMerchantOutletOrDriverDto> getOrderFlowCountForMerchantOrOutletOrDriver(

            @Parameter(description = "Merchant ID. Provide only one of merchantId, outletId or driverId.", example = "50") @RequestParam(value = "merchantId", required = false) Integer merchantId,

            @Parameter(description = "Outlet ID. Provide only one of merchantId, outletId or driverId.", example = "13") @RequestParam(value = "outletId", required = false) Integer outletId,

            @Parameter(description = "Driver ID. Provide only one of merchantId, outletId or driverId.", example = "15") @RequestParam(value = "driverId", required = false) Integer driverId) {

        log.info("GET /getOrderFlowCountForMerchantOrOutletOrDriver called. " + "merchantId={}, outletId={}, driverId={}", merchantId, outletId, driverId);

        // ============================================================
        // Validate that exactly ONE parameter is provided
        // ============================================================

        int providedParameters = 0;

        if (merchantId != null) {
            providedParameters++;
        }

        if (outletId != null) {
            providedParameters++;
        }

        if (driverId != null) {
            providedParameters++;
        }

        // No parameter provided
        if (providedParameters == 0) {

            log.warn("Request rejected. None of merchantId, outletId or driverId provided");

            throw new IllegalArgumentException("Either merchantId, outletId or driverId must be provided");
        }

        // More than one parameter provided
        if (providedParameters > 1) {

            log.warn("Request rejected. Multiple identifiers provided. " + "merchantId={}, outletId={}, driverId={}", merchantId, outletId, driverId);

            throw new IllegalArgumentException("Only one of merchantId, outletId or driverId can be provided");
        }

        // ============================================================
        // Call service
        // ============================================================

        CoOrderFlowCountForMerchantOutletOrDriverDto response = customerService.getOrderFlowCountForMerchantOrOutletOrDriver(merchantId, outletId, driverId);

        log.info("GET /getOrderFlowCountForMerchantOrOutletOrDriver completed successfully. " + "merchantId={}, outletId={}, driverId={}", merchantId, outletId, driverId);

        return ResponseEntity.ok(response);
    }

    //    ==============================================================================
//    ==============================================================================
    @GetMapping("/getOrderDetailsOfOutlet")
    @Operation(summary = "Get order details of outlet", description = """
            Fetches all order details for the given outlet ID.
            
            The merchant total price is calculated from the
            order_items table using merchant_total_price.
            
            For each order, merchant_total_price values of all
            order items belonging to that order are added together.
            
            Example:
            Order jippy202609013 has two order items:
            
            merchant_total_price = 150.00
            merchant_total_price = 10.00
            
            Merchant total price = 160.00
            """)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Order details fetched successfully"), @ApiResponse(responseCode = "400", description = "Invalid outlet ID"), @ApiResponse(responseCode = "404", description = "No orders found for the outlet"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<Page<CoOrderDetailsOfOutletDto>> getOrderDetailsOfOutlet(@RequestParam Integer outletId, Pageable pageable) {

        return ResponseEntity.ok(customerService.getOrderDetailsOfOutlet(outletId, pageable));
    }

    //    ===================================================================================
//    ===================================================================================
    @GetMapping("/getOrderDetailsOfDriver")
    @Operation(summary = "Get order details of driver", description = """
            Fetches paginated order details assigned to a specific driverID.
            
            Driver ID is used to find orders from the Customer & Order
            microservice. Driver charges are calculated using pickup
            charges and driver delivery fee from order price breakup.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver order details fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No orders found for the driver"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<Page<CoOrderDetailsOfDriverDto>>
                        getOrderDetailsOfDriver(@RequestParam
                            @Parameter(description = "Driver ID", example = "15")
                            Integer driverId,
                            Pageable pageable) {

        log.info("Received request to fetch orders for driverId={}", driverId);

        Page<CoOrderDetailsOfDriverDto> response
                    = customerService.getOrderDetailsOfDriver(driverId, pageable);

        return ResponseEntity.ok(response);
    }
}