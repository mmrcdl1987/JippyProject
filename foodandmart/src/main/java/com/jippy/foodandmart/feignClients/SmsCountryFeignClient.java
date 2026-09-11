package com.jippy.foodandmart.feignClients;

import com.jippy.foodandmart.config.SmsCountryFeignConfig;
import com.jippy.foodandmart.dto.SmsCountryRequestDto;
import com.jippy.foodandmart.dto.SmsCountryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "sms-country-client",
        url = "${sms-country.base-url}",
        configuration = SmsCountryFeignConfig.class
)
public interface SmsCountryFeignClient {

    @PostMapping(
            value = "/Accounts/{authKey}/SMSes/"
    )
    SmsCountryResponseDto sendSms(

            @PathVariable("authKey")
            String authKey,

            @RequestBody
            SmsCountryRequestDto request
    );
}