package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.config.SmsCountryFeignConfig;
import com.jippy.customerandorder.dto.SmsCountryRequestDto;
import com.jippy.customerandorder.dto.SmsCountryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "sms-country-client",
        url = "${smscountry.base-url}",
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
            SmsCountryRequestDto request);
}