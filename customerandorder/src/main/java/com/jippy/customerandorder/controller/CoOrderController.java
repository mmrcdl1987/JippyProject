package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.iservice.IOrderService;

import com.jippy.customerandorder.projection.CoDriverEarningsProjection;
import com.jippy.customerandorder.repository.CoOrderPriceBreakupRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/co")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CoOrderController {

    private final IOrderService orderService;
    private final CoOrderRepository coOrderRepository;
    private final CoOrderPriceBreakupRepository coOrderPriceBreakupRepository;

    /*
     * PLACE ORDER
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<CoPlaceOrderResponseDto> placeOrder(@Valid @RequestBody CoPlaceOrderRequestDto requestDto) {

        log.info("API_START | PLACE_ORDER | customerId={} | orderType={}", requestDto.getCustomerId(), requestDto.getOrderType());

        CoPlaceOrderResponseDto response = orderService.placeOrder(requestDto);

        log.info("API_END | PLACE_ORDER_SUCCESS | totalOrdersCreated={}", response.getTotalOrdersCreated());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "Hello from CoOrderController!";

    }

    @PutMapping("/orders/{orderId}/deliver")
    public String deliverOrder(@PathVariable String orderId, @RequestParam Integer driverId) {

        CoOrder order = coOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate order belongs to driver
        if (!order.getDriverId().equals(driverId)) {

            throw new ResourceNotFoundException("Order does not belong to driver");
        }

        // Update status
        order.setOrderStatus(COConstants.STATUS_DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());
        order.setCreatedBy(driverId);


        coOrderRepository.save(order);

        return "Order delivered successfully";
    }

    /*@GetMapping("/driver/earnings")
    public CoDriverEarningsDto fetchDriverEarnings(@RequestParam Integer driverId, @RequestParam LocalDate date) {

        CoDriverEarningsProjection projection = coOrderRepository.fetchDriverEarnings(driverId, date);

        CoDriverEarningsDto dto = new CoDriverEarningsDto();

        dto.setDriverId(driverId);

        dto.setCurrentDate(date);

        dto.setOrdersCountToday(projection.getOrdersCount());

        dto.setTotalEarningsToday(projection.getTotalEarnings());

        return dto;
    }*/
    @GetMapping("/fetchEarnings")
    public CoDriverEarningsDto fetchDriverEarnings(
            @RequestParam Integer driverId,
            @RequestParam LocalDate date
    ) {

        log.info("FETCH_DRIVER_EARNINGS_API_START | driverId={} | date={}",
                driverId,
                date);

        CoDriverEarningsProjection projection =
                coOrderRepository.fetchDriverEarnings(driverId, date);

        CoDriverEarningsDto dto = new CoDriverEarningsDto();

        dto.setTotalEarningsToday(projection.getTotalEarnings());

        dto.setOrdersCountToday(projection.getOrdersCount());

        log.info("FETCH_DRIVER_EARNINGS_API_SUCCESS | driverId={}",
                driverId);

        return dto;
    }

    @GetMapping("/orders/price-breakup")
    public CoOrderPriceBreakupDto getOrderPriceBreakup(@RequestParam String orderId) {

        CoOrderPriceBreakup breakup = coOrderPriceBreakupRepository.findByOrderId(orderId);

        CoOrderPriceBreakupDto dto = new CoOrderPriceBreakupDto();

        dto.setOrderId(breakup.getOrderId());

        dto.setOrderAmount(breakup.getOrderAmount());

        dto.setPlatformFee(breakup.getPlatformFee());

        dto.setDeliveryFee(breakup.getDeliveryFee());

        dto.setSurgeFee(breakup.getSurgeFee());

        dto.setPackagingFee(breakup.getPackagingFee());

        dto.setGst(breakup.getGst());

        dto.setOrderTotalAmount(breakup.getOrderTotalAmount());

        dto.setCouponDiscount(breakup.getCouponDiscount());

        return dto;
    }

    @GetMapping("/orders")
    public CoOrderDto getOrder(@RequestParam String orderId) {

        CoOrderDto dto = orderService.getOrder(orderId);
        return dto;
    }

    //    to get frequent orders >=3 times it will be considered as frequent order and return the outlet id
    @GetMapping("/frequent")
    public List<Integer> getFrequentOutlets(
            @Positive(message = "Customer ID must be a positive integer")
            @RequestParam Integer customerId) {

        log.info("frequent is more than 3 times when th customer order from same outlet" +
                " and return the outlet id for customerId={}", customerId);
        log.info("Fetching frequent outlets for customerId={}", customerId);

        return orderService.getFrequentOutlets(customerId);
    }

    //    to get recent order based on the date that customer ordered
    @GetMapping("/recent")
    public Integer getRecentOutlet(@RequestParam Integer customerId) {

        log.info("Fetching recent outlet for customerId={}", customerId);

        return orderService.getRecentOutlet(customerId);
    }

    @PutMapping("/updateOrderStatus")
    public ResponseEntity<String> updateOrderStatus(@RequestBody CoOrderDto orderDto) {

        log.info("API_START | UPDATE_ORDER_STATUS | orderId={} | newStatus={}", orderDto.getOrderId(), orderDto.getOrderStatus());

        orderService.updateOrderStatus(orderDto);

        log.info("API_END | UPDATE_ORDER_STATUS_SUCCESS | orderId={}", orderDto.getOrderId());

        return ResponseEntity.ok("Order status updated successfully");
    }

}
