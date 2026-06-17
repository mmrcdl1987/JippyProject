package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.exception.SmsFailedException;
import com.jippy.customerandorder.feignClients.SmsCountryFeignClient;
import com.jippy.customerandorder.config.SmsCountryProperties;
import com.jippy.customerandorder.dto.SmsCountryRequestDto;
import com.jippy.customerandorder.dto.SmsCountryResponseDto;
import com.jippy.customerandorder.iservice.SmsCountryService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsCountryServiceImpl implements SmsCountryService {

    private final SmsCountryFeignClient smsCountryFeignClient;
    private final SmsCountryProperties properties;

    @Override
    public String sendOtp(String mobileNumber, String otp) {

        log.info("SMS_SERVICE | SEND_OTP | mobile={} | START", mobileNumber);

        try {

            SmsCountryRequestDto request = new SmsCountryRequestDto();
            request.setNumber(mobileNumber);
            request.setSenderId(properties.getSenderId());

            String message = String.format("Your OTP for jippymart login is %s. Please do not share this OTP with anyone. It is valid for the next 10 minutes-jippymart.in.", otp);

            request.setText(message);

            SmsCountryResponseDto response = smsCountryFeignClient.sendSms(properties.getAuthKey(), request);

            log.info("SMS RESPONSE={}", response);

            String referenceId = null;

            if (response != null) {

                log.info("messageUUID={}", response.getMessageUUID());

                log.info("status={}", response.getStatus());

                log.info("message={}", response.getMessage());

                referenceId = response.getMessageUUID();
            }

            if (referenceId == null || referenceId.isBlank()) {

                referenceId = java.util.UUID.randomUUID().toString();

                log.warn("SMSCountry did not return reference id. Generated UUID={}", referenceId);
            }

            log.info("SMS_SERVICE | SEND_OTP | mobile={} | referenceId={}", mobileNumber, referenceId);

            return referenceId;

        } catch (FeignException ex) {

            log.error("SMS_SERVICE | SEND_OTP | mobile={} | ERROR | status={} | body={}", mobileNumber, ex.status(), ex.contentUTF8(), ex);

            throw new SmsFailedException("Unable to send OTP. Please try again later.");
        }
    }
}