package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.config.FeignClientConfig;
import com.jippy.customerandorder.dto.CoDeviceTokenRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification", configuration = FeignClientConfig.class)
public interface NotificationFeignClient {

    @PostMapping("/api/notification/device-token")
    void saveDeviceToken(@RequestBody CoDeviceTokenRequestDto request);
}
