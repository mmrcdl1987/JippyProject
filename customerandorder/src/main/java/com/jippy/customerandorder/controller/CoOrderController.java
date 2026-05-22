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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co")
@RequiredArgsConstructor
@Slf4j
public class CoOrderController {

    private final IOrderService orderService;
    private final CoOrderRepository coOrderRepository;
    private final CoOrderPriceBreakupRepository coOrderPriceBreakupRepository;

    @PostMapping("/placeOrder")
    public ResponseEntity<CoPlaceOrderResponseDto> placeOrder(@Valid @RequestBody CoPlaceOrderRequestDto placeOrderRequestDto) {

        log.info("API hit: place order | customerId={}, outletId={}", placeOrderRequestDto.getCustomerId(), placeOrderRequestDto.getOutletId());

        CoPlaceOrderResponseDto response = orderService.placeOrder(placeOrderRequestDto);

        log.info("Order placed successfully | customerId={}, outletId={}, orderId={}", placeOrderRequestDto.getCustomerId(), placeOrderRequestDto.getOutletId(), response.getOrderId());

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

        coOrderRepository.save(order);

        return "Order delivered successfully";
    }

    @GetMapping("/driver/earnings")
    public CoDriverEarningsDto fetchDriverEarnings(@RequestParam Integer driverId, @RequestParam LocalDate date) {

        CoDriverEarningsProjection projection = coOrderRepository.fetchDriverEarnings(driverId, date);

        CoDriverEarningsDto dto = new CoDriverEarningsDto();

        dto.setDriverId(driverId);

        dto.setCurrentDate(date);

        dto.setOrdersCountToday(projection.getOrdersCount());

        dto.setTotalEarningsToday(projection.getTotalEarnings());

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

        CoOrder order = coOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        CoOrderDto dto = new CoOrderDto();

        dto.setOrderId(order.getOrderId());

        dto.setDriverId(order.getDriverId());

        dto.setOrderStatus(order.getOrderStatus());

        dto.setPaymentModeId(order.getPaymentModeId());

        return dto;
    }

    //    to get frequent orders >=3 times
    @GetMapping("/frequent")
    public List<Integer> getFrequentOutlets(@RequestParam Integer customerId) {

        log.info("Fetching frequent outlets for customerId={}", customerId);

        return orderService.getFrequentOutlets(customerId);
    }

    //    to get recent order based on the date that customer ordered
    @GetMapping("/recent")
    public Integer getRecentOutlet(@RequestParam Integer customerId) {

        log.info("Fetching recent outlet for customerId={}", customerId);

        return orderService.getRecentOutlet(customerId);
    }

}
