package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoOrderRejectionRequestDto;
import com.jippy.customerandorder.entity.CoOrderRejection;
import com.jippy.customerandorder.iservice.CoOrderRejectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: CoOrderRejectionController
 */
@RestController
@RequestMapping("/api/co/order-rejections")
public class CoOrderRejectionController {

    @Autowired
    private CoOrderRejectionService service;

    @PostMapping("/reject")
    public ResponseEntity<CoOrderRejection> rejectOrder(
            @RequestBody CoOrderRejectionRequestDto request) {

        return ResponseEntity.ok(service.rejectOrder(request));
    }
}