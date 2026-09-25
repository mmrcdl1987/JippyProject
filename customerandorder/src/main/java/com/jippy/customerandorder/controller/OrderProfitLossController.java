package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderProfitLossDto;
import com.jippy.customerandorder.enums.DateRangeFilter;
import com.jippy.customerandorder.iservice.CoOrderProfitLossService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/co")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderProfitLossController {

    private final CoOrderProfitLossService orderService;

    @GetMapping("/orders/profit-loss")
    public CoOrderProfitLossDto getOrderProfitLoss(
            @RequestParam String orderId) {

        log.info(
                "API_START | GET_ORDER_PROFIT_LOSS | orderId={}",
                orderId
        );

        CoOrderProfitLossDto response =
                orderService.getOrderProfitLoss(orderId);

        log.info(
                "API_END | GET_ORDER_PROFIT_LOSS_SUCCESS | " +
                        "orderId={} | status={}",
                orderId,
                response.getProfitLossStatus()
        );

        return response;
    }


    @GetMapping("/orders/profit-loss/all")
    public List<CoOrderProfitLossDto> getAllOrdersProfitLoss(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) DateRangeFilter dateRangeFilter) {

        log.info(
                "API_START | GET_ALL_ORDERS_PROFIT_LOSS | fromDate={} | toDate={} | dateRangeFilter={}",
                fromDate,
                toDate,
                dateRangeFilter
        );

        List<CoOrderProfitLossDto> response =
                orderService.getAllOrdersProfitLoss(
                        fromDate,
                        toDate,
                        dateRangeFilter
                );

        log.info(
                "API_END | GET_ALL_ORDERS_PROFIT_LOSS_SUCCESS | totalOrders={}",
                response.size()
        );

        return response;
    }
}