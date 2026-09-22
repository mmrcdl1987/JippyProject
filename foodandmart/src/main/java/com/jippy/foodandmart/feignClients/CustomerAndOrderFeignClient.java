    package com.jippy.foodandmart.feignClients;

    import com.jippy.foodandmart.dto.*;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;

    @FeignClient(name = "CUSTOMERANDORDER")
    public interface CustomerAndOrderFeignClient {
        @GetMapping("/api/co/driver/getDriverDetails")
        DriverResponseDto getDriverDetails(@RequestParam Integer driverId);

        @GetMapping("/api/co/customers/{customerId}")
        CustomerResponseDto getCustomer(@PathVariable Integer customerId);

    //    to fetch frequent orders for a customer, we can fetch the order history for that customer
    //    and then calculate the frequency of orders for each outlet. Based on the frequency,
    //    we can return the list of outletIds that are most frequently ordered from by that customer.
    //    from FM microservice
        @GetMapping("/api/co/frequent")
        List<Integer> getFrequentOutlets(@RequestParam Integer customerId);

        @GetMapping("/api/co/recent")
        Integer getRecentOutlet(@RequestParam Integer customerId);

        /**
         * Fetches settlement calculation from
         * Customer and Order microservice.
         * <p>
         * CO returns:
         * - merchantTotalPrice
         * - promotionDeductedAmount
         * <p>
         * FM uses these values to calculate GST
         * and the final settlement amount.
         */
        @GetMapping("/api/co/settlements/calculateSettlementForOutlet")
        CoSettlementCalculationDto calculateSettlementForOutlet(
                @RequestParam("outletId") Integer outletId
        );

        /**
         * Fetches merchant settlement calculation details
         * from Customer and Order microservice
         * for the selected outlets and settlement period.
         */
        @PostMapping("/api/co/settlements/getMerchantSettlement")
        List<CoMerchantSettlementSummaryDto> getMerchantSettlement(
                @RequestBody CoMerchantSettlementBetweenDatesRequestDto requestDto
        );
        /**
         * Fetches outlet and order settlement details
         * from Customer and Order microservice
         * for the selected settlement period.
         *
         * CO calculates:
         * - Number of unique outlets
         * - Number of delivered orders
         * - Total merchant amount
         * - Total merchant promotion deduction
         *
         * FM uses these values for further settlement
         * and GST calculation.
         */
        @GetMapping("/api/co/settlements/getOutletAndOrdersSettlementDetails")
        FmOutletAndOrdersCountForSettlementResponseDto getOutletAndOrdersSettlementDetails(
                @RequestParam("startDate") LocalDate startDate,
                @RequestParam("endDate") LocalDate endDate
        );


        /**
         * Fetches merchant settlement calculation details
         * from Customer and Order microservice.
         *
         * FM sends the already-resolved settlement dates.
         * CO calculates the order-related settlement values.
         */
        @PostMapping("/api/co/settlements/getMerchantSettlementForOutletBetweenDates")
        CoMerchantSettlementSummaryDto getMerchantSettlementForOutletBetweenDates(
                @RequestBody CoMerchantSettlementForOutletRequestDto requestDto
        );
    @GetMapping("/api/co/orderSummaryForOutlet")
    public ResponseEntity<List<FmOrderSummaryDto>> orderSummaryForOutlet(
            @RequestParam Integer outletId, Pageable pageable);
        /**
         * Fetches delivered order-item settlement details
         * from Customer & Order service.
         *
         * CO provides:
         * - orderId
         * - createdAt
         * - productId
         * - variantOptionId
         * - quantity
         * - merchantTotalPrice
         * - promotionDeductedAmount
         * - discountType
         */
        @GetMapping("/api/co/settlements/orderDetailsForOutlet")
        List<FmMerchantSettlementOrderDetailsDto> getMerchantSettlementOrderDetails(
                @RequestParam("outletId") Integer outletId,
                @RequestParam("startDate") LocalDateTime startDate,
                @RequestParam("endDate") LocalDateTime endDate
        );
}


