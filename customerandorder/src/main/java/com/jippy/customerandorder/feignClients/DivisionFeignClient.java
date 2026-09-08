package com.jippy.customerandorder.feignClients;

import com.jippy.customerandorder.dto.CoRefundDetailsDto;
import com.jippy.customerandorder.dto.WelcomeCouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "division")
public interface DivisionFeignClient {

    @GetMapping("/api/div/coupons/welcome")
    ResponseEntity<List<WelcomeCouponDto>> getActiveWelcomeCoupons();

    @PostMapping("/api/div/payment/refund/orderRefund")
     ResponseEntity<String> orderRefund(@RequestParam String orderId,
            @RequestParam String reason);

//    =================================================================================
//    =================================================================================
        // =========================================================
        // Get refund details from Division service
        // =========================================================

        @GetMapping("/api/div/payment/refund/getRefundDetails")
        CoRefundDetailsDto getRefundDetails(
                @RequestParam("orderId") String orderId
        );

}