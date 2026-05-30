package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderRejection;
import com.jippy.customerandorder.iservice.CoCustomerDeliveryService;
import com.jippy.customerandorder.iservice.CoOrderRejectionService;

import com.jippy.customerandorder.repository.CoOrderRejectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/co/order-rejections")
public class CoOrderRejectionController {

    @Autowired
    private CoOrderRejectionService service;
    @Autowired
    private CoOrderRejectionRepository coOrderRejectionRepository;
    @Autowired
    private CoCustomerDeliveryService customerDeliveryService;

    @PostMapping("/reject")
    public ResponseEntity<CoOrderRejection> rejectOrder(@RequestBody CoOrderRejectionRequestDto request) {

        return ResponseEntity.ok(service.rejectOrder(request));
    }

    @GetMapping("/driver/rejected-orders/count")
    public Long fetchRejectedOrdersCount(@RequestParam Integer driverId) {

        return coOrderRejectionRepository.fetchRejectedOrdersCount(driverId);
    }


    @PostMapping("/customer-unreachable")
    public CoCustomerUnreachableResponseDto customerUnreachable(@RequestBody CoCustomerUnreachableRequestDto requestDto) {

        log.info("Customer unreachable API request received for orderId : {}", requestDto.getOrderId());

        return customerDeliveryService.customerUnreachable(requestDto);
    }

    @PostMapping("/final-reject")
    public ResponseEntity<CoFinalRejectResponseDto> finalRejectOrder(@RequestBody CoFinalRejectRequestDto requestDto) {

        log.info("Final reject API called for orderId : {}", requestDto.getOrderId());

        return ResponseEntity.ok(customerDeliveryService.finalRejectOrder(requestDto));
    }
}