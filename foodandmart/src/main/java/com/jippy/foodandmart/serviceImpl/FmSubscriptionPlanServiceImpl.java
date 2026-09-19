package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmSubscriptionPlanRequestDto;
import com.jippy.foodandmart.dto.FmSubscriptionPlanResponseDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;
import com.jippy.foodandmart.entity.FmArea;
import com.jippy.foodandmart.entity.FmCity;
import com.jippy.foodandmart.entity.FmState;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.SubscriptionPlanMapper;
import com.jippy.foodandmart.repository.FmAreaRepository;
import com.jippy.foodandmart.repository.FmCityRepository;
import com.jippy.foodandmart.repository.FmStateRepository;
import com.jippy.foodandmart.repository.FmSubscriptionPlanRepository;
import com.jippy.foodandmart.service.IFmSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmSubscriptionPlanServiceImpl implements IFmSubscriptionPlanService {

    private final FmSubscriptionPlanRepository repository;
    private final FmAreaRepository areaRepository;
    private final FmCityRepository cityRepository;
    private final FmStateRepository stateRepository;

    @Override
    public SubscriptionPlanResponseDto saveOrUpdate(FmSubscriptionPlanRequestDto request) {

        String operationId = System.nanoTime() + "";
        MDC.put("operationId", operationId);
        MDC.put("planId", String.valueOf(request.getSubscriptionPlanId()));

        try {
            log.info("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | START | planId={}", request.getSubscriptionPlanId());

            // Validate request
            if (request == null) {
                log.error("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | VALIDATION_FAILED | request is null");
                throw new IllegalArgumentException("Request body cannot be null");
            }

            FmSubscriptionPlan plan;
            boolean isCreate = request.getSubscriptionPlanId() == null;

            if (isCreate) {
                log.debug("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | ACTION | creating new plan | planName={}", request.getPlanName());
                plan = SubscriptionPlanMapper.toEntity(request);

            } else {
                log.debug("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | ACTION | updating existing plan | planId={}", request.getSubscriptionPlanId());
                plan = repository.findById(request.getSubscriptionPlanId())
                        .orElseThrow(() -> {
                            log.warn("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | NOT_FOUND | planId={}", request.getSubscriptionPlanId());
                            return new ResourceNotFoundException("Subscription plan not found with id: " + request.getSubscriptionPlanId());
                        });
                SubscriptionPlanMapper.updateEntity(plan, request);
            }

            log.debug("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | DATABASE | persisting to database");
            FmSubscriptionPlan savedPlan = repository.saveAndFlush(plan);

            log.info("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | SUCCESS | operation={} | planId={} | action={}", 
                    operationId, savedPlan.getSubscriptionPlanId(), isCreate ? "CREATE" : "UPDATE");

            return mapToSubscriptionPlanResponseDto(savedPlan);

        } catch (Exception ex) {
            log.error("FM_SUBSCRIPTION_PLAN | SAVE_OR_UPDATE | ERROR | operationId={} | exception={}", 
                    operationId, ex.getClass().getSimpleName(), ex);
            throw ex;

        } finally {
            MDC.remove("operationId");
            MDC.remove("planId");
        }
    }

    @Override
    public SubscriptionPlanResponseDto getById(Integer subscriptionPlanId) {

        String operationId = System.nanoTime() + "";
        MDC.put("operationId", operationId);
        MDC.put("planId", String.valueOf(subscriptionPlanId));

        try {
            log.debug("FM_SUBSCRIPTION_PLAN | GET_BY_ID | START | planId={}", subscriptionPlanId);

            if (subscriptionPlanId == null) {
                log.warn("FM_SUBSCRIPTION_PLAN | GET_BY_ID | VALIDATION_FAILED | planId is null");
                throw new IllegalArgumentException("Subscription plan ID cannot be null");
            }

            FmSubscriptionPlan plan = repository.findById(subscriptionPlanId)
                    .orElseThrow(() -> {
                        log.warn("FM_SUBSCRIPTION_PLAN | GET_BY_ID | NOT_FOUND | planId={}", subscriptionPlanId);
                        return new ResourceNotFoundException("Subscription plan not found with id: " + subscriptionPlanId);
                    });

            log.debug("FM_SUBSCRIPTION_PLAN | GET_BY_ID | SUCCESS | operationId={} | planId={}", operationId, subscriptionPlanId);
            return mapToSubscriptionPlanResponseDto(plan);

        } catch (Exception ex) {
            log.error("FM_SUBSCRIPTION_PLAN | GET_BY_ID | ERROR | operationId={} | planId={} | exception={}", 
                    operationId, subscriptionPlanId, ex.getClass().getSimpleName(), ex);
            throw ex;

        } finally {
            MDC.remove("operationId");
            MDC.remove("planId");
        }
    }

    @Override
    public List<SubscriptionPlanResponseDto> getAll() {

        String operationId = System.nanoTime() + "";
        MDC.put("operationId", operationId);

        try {
            log.debug("FM_SUBSCRIPTION_PLAN | GET_ALL | START");

            List<FmSubscriptionPlan> plans = repository.findAll();
            log.debug("FM_SUBSCRIPTION_PLAN | GET_ALL | FETCHED | count={}", plans.size());

            List<SubscriptionPlanResponseDto> dtos = mapToSubscriptionPlanResponseDtoList(plans);

            log.info("FM_SUBSCRIPTION_PLAN | GET_ALL | SUCCESS | operationId={} | totalCount={}", operationId, dtos.size());
            return dtos;

        } catch (Exception ex) {
            log.error("FM_SUBSCRIPTION_PLAN | GET_ALL | ERROR | operationId={} | exception={}", 
                    operationId, ex.getClass().getSimpleName(), ex);
            throw ex;

        } finally {
            MDC.remove("operationId");
        }
    }

    @Override
    public void delete(Integer subscriptionPlanId) {

        String operationId = System.nanoTime() + "";
        MDC.put("operationId", operationId);
        MDC.put("planId", String.valueOf(subscriptionPlanId));

        try {
            log.info("FM_SUBSCRIPTION_PLAN | DELETE | START | planId={}", subscriptionPlanId);

            if (subscriptionPlanId == null) {
                log.warn("FM_SUBSCRIPTION_PLAN | DELETE | VALIDATION_FAILED | planId is null");
                throw new IllegalArgumentException("Subscription plan ID cannot be null");
            }

            FmSubscriptionPlan plan = repository.findById(subscriptionPlanId)
                    .orElseThrow(() -> {
                        log.warn("FM_SUBSCRIPTION_PLAN | DELETE | NOT_FOUND | planId={}", subscriptionPlanId);
                        return new ResourceNotFoundException("Subscription plan not found with id: " + subscriptionPlanId);
                    });

            log.debug("FM_SUBSCRIPTION_PLAN | DELETE | DATABASE | removing from database");
            repository.delete(plan);

            log.info("FM_SUBSCRIPTION_PLAN | DELETE | SUCCESS | operationId={} | planId={}", operationId, subscriptionPlanId);

        } catch (Exception ex) {
            log.error("FM_SUBSCRIPTION_PLAN | DELETE | ERROR | operationId={} | planId={} | exception={}", 
                    operationId, subscriptionPlanId, ex.getClass().getSimpleName(), ex);
            throw ex;

        } finally {
            MDC.remove("operationId");
            MDC.remove("planId");
        }
    }

    @Override
    public FmApiResponse<List<FmSubscriptionPlanResponseDto>> getSubscriptionPlansByAreaId(Integer areaId) {

        log.info("Received request to fetch subscription plans for Area Id: {}", areaId);

        if (areaId == null || areaId <= 0) {
            log.error("Invalid Area Id received: {}", areaId);
            throw new IllegalArgumentException("Area Id must be greater than zero.");
        }

        log.debug("Fetching subscription plans from database for Area Id: {}", areaId);

        List<FmSubscriptionPlan> plans = repository.findByAreaIdOrderByPriceAsc(areaId);

        if (plans == null || plans.isEmpty()) {

            log.warn("No subscription plans found for Area Id: {}", areaId);
            log.info("Returning successful response with no subscription plans for Area Id: {}", areaId);

            return FmApiResponse.success(
                    "No subscription plans found for the selected area.",
                    Collections.emptyList()
            );
        }

        List<FmSubscriptionPlanResponseDto> response = mapToFmSubscriptionPlanResponseDtoList(plans);

        log.info("Successfully fetched {} subscription plan(s) for Area Id: {}",
                response.size(), areaId);

        log.debug("Subscription plan response prepared successfully for Area Id: {}", areaId);

        return FmApiResponse.success(
                "Subscription plans fetched successfully.",
                response
        );
    }

    private SubscriptionPlanResponseDto mapToSubscriptionPlanResponseDto(FmSubscriptionPlan plan) {
        if (plan == null) return null;
        FmArea area = null;
        FmCity city = null;
        FmState state = null;

        if (plan.getAreaId() != null) {
            area = areaRepository.findById(plan.getAreaId()).orElse(null);
            if (area != null && area.getCityId() != null) {
                city = cityRepository.findById(area.getCityId()).orElse(null);
                if (city != null && city.getStateId() != null) {
                    state = stateRepository.findById(city.getStateId()).orElse(null);
                }
            }
        }
        return SubscriptionPlanMapper.toDto(plan, area, city, state);
    }

    private List<SubscriptionPlanResponseDto> mapToSubscriptionPlanResponseDtoList(List<FmSubscriptionPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> areaIds = plans.stream()
                .map(FmSubscriptionPlan::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmArea> areaMap = areaIds.isEmpty() ? Collections.emptyMap() :
                areaRepository.findAllById(areaIds).stream()
                        .collect(Collectors.toMap(FmArea::getAreaId, a -> a, (a1, a2) -> a1));

        Set<Integer> cityIds = areaMap.values().stream()
                .map(FmArea::getCityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmCity> cityMap = cityIds.isEmpty() ? Collections.emptyMap() :
                cityRepository.findAllById(cityIds).stream()
                        .collect(Collectors.toMap(FmCity::getCityId, c -> c, (c1, c2) -> c1));

        Set<Integer> stateIds = cityMap.values().stream()
                .map(FmCity::getStateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmState> stateMap = stateIds.isEmpty() ? Collections.emptyMap() :
                stateRepository.findAllById(stateIds).stream()
                        .collect(Collectors.toMap(FmState::getStateId, s -> s, (s1, s2) -> s1));

        return plans.stream().map(plan -> {
            FmArea area = plan.getAreaId() != null ? areaMap.get(plan.getAreaId()) : null;
            FmCity city = (area != null && area.getCityId() != null) ? cityMap.get(area.getCityId()) : null;
            FmState state = (city != null && city.getStateId() != null) ? stateMap.get(city.getStateId()) : null;
            return SubscriptionPlanMapper.toDto(plan, area, city, state);
        }).collect(Collectors.toList());
    }

    private List<FmSubscriptionPlanResponseDto> mapToFmSubscriptionPlanResponseDtoList(List<FmSubscriptionPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> areaIds = plans.stream()
                .map(FmSubscriptionPlan::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmArea> areaMap = areaIds.isEmpty() ? Collections.emptyMap() :
                areaRepository.findAllById(areaIds).stream()
                        .collect(Collectors.toMap(FmArea::getAreaId, a -> a, (a1, a2) -> a1));

        Set<Integer> cityIds = areaMap.values().stream()
                .map(FmArea::getCityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmCity> cityMap = cityIds.isEmpty() ? Collections.emptyMap() :
                cityRepository.findAllById(cityIds).stream()
                        .collect(Collectors.toMap(FmCity::getCityId, c -> c, (c1, c2) -> c1));

        Set<Integer> stateIds = cityMap.values().stream()
                .map(FmCity::getStateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, FmState> stateMap = stateIds.isEmpty() ? Collections.emptyMap() :
                stateRepository.findAllById(stateIds).stream()
                        .collect(Collectors.toMap(FmState::getStateId, s -> s, (s1, s2) -> s1));

        return plans.stream().map(plan -> {
            FmArea area = plan.getAreaId() != null ? areaMap.get(plan.getAreaId()) : null;
            FmCity city = (area != null && area.getCityId() != null) ? cityMap.get(area.getCityId()) : null;
            FmState state = (city != null && city.getStateId() != null) ? stateMap.get(city.getStateId()) : null;
            return SubscriptionPlanMapper.toFmDto(plan, area, city, state);
        }).collect(Collectors.toList());
    }
}