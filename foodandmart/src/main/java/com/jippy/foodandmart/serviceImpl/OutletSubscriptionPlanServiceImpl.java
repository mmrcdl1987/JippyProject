package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.OutletSubscriptionResponseDto;
import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.OutletSubscriptionPlanMapper;
import com.jippy.foodandmart.repository.FmSubscriptionPlanRepository;
import com.jippy.foodandmart.repository.OutletSubscriptionPlanRepository;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutletSubscriptionPlanServiceImpl implements OutletSubscriptionPlanService {

    private final OutletSubscriptionPlanRepository outletSubscriptionPlanRepository;
    private final FmSubscriptionPlanRepository fmSubscriptionPlanRepository;
    private final OutletSubscriptionPlanMapper outletSubscriptionPlanMapper;

    @Override
    public OutletSubscriptionResponseDto getOutletSubscription(Integer outletId) {

        log.info("SERVICE_START | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId);

        try {

            FmOutletSubscriptionPlan subscription = outletSubscriptionPlanRepository.findTopByOutletIdOrderBySubscriptionToDateDesc(outletId).orElseThrow(() -> {

                log.warn("No subscription found | outletId={}", outletId);

                return new ResourceNotFoundException("No subscription found for outlet");
            });

            FmSubscriptionPlan plan = fmSubscriptionPlanRepository.findById(subscription.getSubscriptionPlanId()).orElseThrow(() -> {

                log.warn("Subscription plan not found | planId={}", subscription.getSubscriptionPlanId());

                return new ResourceNotFoundException("Subscription plan not found");
            });

            OutletSubscriptionResponseDto response = outletSubscriptionPlanMapper.toResponseDto(subscription, plan);

            boolean active = !subscription.getSubscriptionToDate().isBefore(LocalDate.now());

            response.setActive(active);

            long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), subscription.getSubscriptionToDate());

            response.setRemainingDays(Math.max(remainingDays, 0));

            log.info("SERVICE_SUCCESS | GET_OUTLET_SUBSCRIPTION | outletId={} | active={}", outletId, active);

            return response;

        } catch (Exception ex) {

            log.error("SERVICE_ERROR | GET_OUTLET_SUBSCRIPTION | outletId={}", outletId, ex);

            throw ex;
        }
    }
}