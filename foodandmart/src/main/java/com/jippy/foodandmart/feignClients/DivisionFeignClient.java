package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.dto.FmDivPriceModelDto;
import com.jippy.foodandmart.dto.FmSendOtpMailRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "division")
public interface DivisionFeignClient {

   @GetMapping("/api/coupons/getPriceModels")
    public ResponseEntity<List<FmDivPriceModelDto>> getPriceModels();

    @PostMapping("/api/div/email/sendOtp")
    public ResponseEntity<String> sendOtpMail(@RequestBody FmSendOtpMailRequestDto requestDto);
}
