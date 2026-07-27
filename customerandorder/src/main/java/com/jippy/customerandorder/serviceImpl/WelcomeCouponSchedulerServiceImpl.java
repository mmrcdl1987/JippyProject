package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.WelcomeCouponDto;
import com.jippy.customerandorder.dto.WelcomeCouponNotificationEvent;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.feignClients.DivisionFeignClient;
import com.jippy.customerandorder.iservice.WelcomeCouponSchedulerService;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.repository.CoCustomerRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import com.jippy.customerandorder.repository.CustomerCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WelcomeCouponSchedulerServiceImpl implements WelcomeCouponSchedulerService {

    private final CoCustomerRepository customerRepository;
    private final CoOrderRepository orderRepository;
    private final CustomerCouponRepository customerCouponRepository;
    private final DivisionFeignClient divisionFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private String welcomeCouponTopic="welcome-coupon";

    @Override
    public void processWelcomeCoupons() {

        log.info("WELCOME_COUPON_SCHEDULER_STARTED");

        List<CoCustomer> customers = customerRepository.findEligibleWelcomeCustomers();

        if (customers.isEmpty()) {

            log.info("No eligible customers found.");

            return;
        }

        log.info("Eligible customers count : {}", customers.size());

        List<WelcomeCouponDto> coupons;

        try {

            ResponseEntity<List<WelcomeCouponDto>> response = divisionFeignClient.getActiveWelcomeCoupons();

            coupons = response.getBody();

        } catch (Exception ex) {

            log.error("Failed to fetch welcome coupons from Division Service.", ex);

            return;
        }

        if (coupons == null || coupons.isEmpty()) {

            log.warn("No active welcome coupons available.");

            return;
        }

        log.info("Active welcome coupons count : {}", coupons.size());

        for (CoCustomer customer : customers) {

            processCustomer(customer, coupons);

        }

        log.info("WELCOME_COUPON_SCHEDULER_COMPLETED");
    }

    /**
     * Process single customer.
     */
    private void processCustomer(CoCustomer customer, List<WelcomeCouponDto> coupons) {

        try {

            log.info("WELCOME_COUPON_PROCESS_START | customerId={}", customer.getCustomerId());

            /*
             * STEP 1 : Check Delivered Order
             */
            boolean hasDeliveredOrder = orderRepository.existsByCustomerIdAndOrderStatus(customer.getCustomerId(), COConstants.ORDER_STATUS_DELIVERED);

            if (hasDeliveredOrder) {

                log.info("Customer already placed a delivered order. customerId={}", customer.getCustomerId());

                return;
            }

            /*
             * STEP 2 : Find Available Coupon
             */
            WelcomeCouponDto coupon = findAvailableCoupon(customer.getCustomerId(), coupons);

            if (coupon == null) {

                log.info("No available welcome coupon found. customerId={}", customer.getCustomerId());

                return;
            }

            /*
             * STEP 3 : Create Kafka Event
             */
            WelcomeCouponNotificationEvent event = COEventMapper.mapToWelcomeCouponEvent(customer, coupon);

            /*
             * STEP 4 : Publish Kafka Event
             */
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(welcomeCouponTopic, customer.getCustomerId().toString(), event);

            future.whenComplete((result, ex) -> {

                if (ex != null) {

                    log.error("WELCOME_COUPON_EVENT_FAILED | customerId={}", customer.getCustomerId(), ex);

                } else {

                    log.info("WELCOME_COUPON_EVENT_PUBLISHED | customerId={} | topic={} | partition={} | offset={}", customer.getCustomerId(), result.getRecordMetadata().topic(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());

                }

            });

            log.info("WELCOME_COUPON_PROCESS_COMPLETED | customerId={}", customer.getCustomerId());

        } catch (Exception ex) {

            log.error("WELCOME_COUPON_PROCESS_FAILED | customerId={}", customer.getCustomerId(), ex);

        }
    }

    /**
     * Find first available welcome coupon.
     */
    private WelcomeCouponDto findAvailableCoupon(Integer customerId, List<WelcomeCouponDto> coupons) {

        for (WelcomeCouponDto coupon : coupons) {

            boolean redeemed = customerCouponRepository.existsByCustomerIdAndCouponIdAndIsRedeemedTrue(customerId, coupon.getCouponId());

            if (!redeemed) {

                log.info("Available coupon found. customerId={}, couponCode={}", customerId, coupon.getCouponCode());

                return coupon;
            }
        }

        log.info("All welcome coupons already redeemed. customerId={}", customerId);

        return null;
    }
}