package com.jippy.driver.controller;

import com.jippy.driver.dto.DriverDto;
import com.jippy.driver.dto.uber.UberDispatchRequestDto;
import com.jippy.driver.serviceImpl.UberDirectClient;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/driver/uber")
public class UberDriverDispatchController {

    private final UberDirectClient uberDirectClient;

    @PostMapping(path = "/dispatchOrderToUber")
    public void dispatchOrderToUber(
            @Valid @org.springframework.web.bind.annotation.RequestBody UberDispatchRequestDto uberDispatchRequestDto) {

        log.info("raw json : {} ", uberDispatchRequestDto);
        // This will automatically invoke getAccessToken(), obtain the token, and send the request
        String uberDeliveryId = uberDirectClient.createDelivery(uberDispatchRequestDto);
        System.out.println("Dispatched to Uber successfully. Delivery ID: " + uberDeliveryId);

    }
}
