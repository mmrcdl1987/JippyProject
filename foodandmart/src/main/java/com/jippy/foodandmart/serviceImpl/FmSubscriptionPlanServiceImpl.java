package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmSubscriptionPlanDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmSubscriptionPlanMapper;
import com.jippy.foodandmart.repository.FmSubscriptionPlanRepository;
import com.jippy.foodandmart.service.IFmSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmSubscriptionPlanServiceImpl implements IFmSubscriptionPlanService {

    private final FmSubscriptionPlanRepository repository;
    private final FmSubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public FmSubscriptionPlanDto createAndUpdatePlans(FmSubscriptionPlanDto dto) {

        // UPDATE if id is present/exists
        if (dto.getSubscriptionPlanId() != null) {

            log.info("Updating subscription plan with id: {}", dto.getSubscriptionPlanId());

            FmSubscriptionPlan existingPlan = repository.findById(dto.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + dto.getSubscriptionPlanId()));

            FmSubscriptionPlanMapper.updateEntity(existingPlan, dto);

            existingPlan.setUpdatedAt(LocalDateTime.now());

            FmSubscriptionPlan updatedPlan = repository.save(existingPlan);

            log.info("Subscription plan updated successfully with id: {}", updatedPlan.getSubscriptionPlanId());

            return FmSubscriptionPlanMapper.mapToDto(updatedPlan);
        }

        // CREATE if id is null
        log.info("Creating new subscription plan");

        FmSubscriptionPlan newPlan = FmSubscriptionPlanMapper.mapToEntity(dto);

        newPlan.setCreatedAt(LocalDateTime.now());

        FmSubscriptionPlan savedPlan = repository.save(newPlan);

        log.info("Subscription plan created successfully with id: {}", savedPlan.getSubscriptionPlanId());

        return FmSubscriptionPlanMapper.mapToDto(savedPlan);
    }


    @Override
    public List<SubscriptionPlanResponseDto> getSubscriptionPlans(Integer areaId) {

        log.info("SERVICE_START | GET_SUBSCRIPTION_PLANS | areaId={}", areaId);

        try {

            log.debug("Fetching subscription plans | areaId={}", areaId);

            List<FmSubscriptionPlan> subscriptionPlans = repository.findByAreaIdOrderByPriceAsc(areaId);

            if (subscriptionPlans.isEmpty()) {

                log.warn("No subscription plans found | areaId={}", areaId);

                throw new ResourceNotFoundException("No subscription plans found for the given area");
            }

            List<SubscriptionPlanResponseDto> response = subscriptionPlans.stream().map(subscriptionPlanMapper::toResponseDto).toList();

            log.info("SERVICE_SUCCESS | GET_SUBSCRIPTION_PLANS | areaId={} | count={}", areaId, response.size());

            return response;

        } catch (Exception ex) {

            log.error("SERVICE_ERROR | GET_SUBSCRIPTION_PLANS | areaId={}", areaId, ex);

            throw ex;
        }
    }


}