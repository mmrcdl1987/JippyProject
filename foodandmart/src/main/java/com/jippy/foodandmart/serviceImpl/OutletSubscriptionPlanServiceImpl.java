package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import com.jippy.foodandmart.exception.BannerUploadException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.OutletSubscriptionPlanMapper;
import com.jippy.foodandmart.projections.ActiveBannerProjection;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmSubscriptionPlanRepository;
import com.jippy.foodandmart.repository.OutletSubscriptionPlanRepository;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutletSubscriptionPlanServiceImpl implements OutletSubscriptionPlanService {

    private final OutletSubscriptionPlanRepository outletSubscriptionPlanRepository;

    private final FmSubscriptionPlanRepository subscriptionPlanRepository;

    private final FmOutletRepository outletRepository;

    private final S3ImageService s3ImageService;

    @Override
    @Transactional
    public OutletSubscriptionPlanResponseDto subscribeOutlet(OutletSubscriptionPlanRequestDto request) {
        String op = "SUBSCRIPTION_CREATE_" + UUID.randomUUID();
        log.info("{} | START | outletId={} | subscriptionPlanId={}", op, request.getOutletId(), request.getSubscriptionPlanId());
        try {
            FmOutlet outlet = outletRepository.findById(request.getOutletId()).orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id : " + request.getOutletId()));

            FmSubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id : " + request.getSubscriptionPlanId()));

            LocalDate subscriptionFromDate = LocalDate.now();
            LocalDate subscriptionToDate = subscriptionFromDate.plusDays(plan.getDurationInDays());
            LocalDate bannerToDate = request.getBannerFromDate().plusDays(plan.getBannerDurationInDays());

            FmOutletSubscriptionPlan entity = new FmOutletSubscriptionPlan();
            entity.setOutletId(outlet.getOutletId());
            entity.setSubscriptionPlanId(plan.getSubscriptionPlanId());
            entity.setSubscriptionFromDate(subscriptionFromDate);
            entity.setSubscriptionToDate(subscriptionToDate);
            entity.setBannerFromDate(request.getBannerFromDate());
            entity.setBannerToDate(bannerToDate);
            entity.setPriceModelType(request.getPriceModelType());
            entity.setOfferAmount(request.getOfferAmount());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setCreatedBy(request.getUserId());

            FmOutletSubscriptionPlan saved = outletSubscriptionPlanRepository.saveAndFlush(entity);

            log.info("{} | SUCCESS | outletSubscriptionPlanId={}", op, saved.getOutletSubscriptionPlanId());

            return OutletSubscriptionPlanMapper.toDto(saved);
        } catch (Exception ex) {
            log.error("{} | ERROR while creating subscription | outletId={} | error={}", op, request.getOutletId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OutletBannerDesignerResponseDto getDesignerDetails(Integer outletId) {
        String op = "DESIGNER_DETAILS_" + UUID.randomUUID();
        log.info("{} | START | outletId={}", op, outletId);
        try {
            FmOutletSubscriptionPlan outletPlan = outletSubscriptionPlanRepository.findByOutletId(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet subscription not found"));

            FmSubscriptionPlan plan = subscriptionPlanRepository.findById(outletPlan.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

            FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet not found with id : " + outletId));

            OutletBannerDesignerResponseDto dto = new OutletBannerDesignerResponseDto();
            dto.setOutletId(outlet.getOutletId());

            dto.setOutletName(outlet.getOutletName());

            dto.setSubscriptionPlanId(plan.getSubscriptionPlanId());

            dto.setPlanName(plan.getPlanName());

            dto.setSubscriptionFromDate(outletPlan.getSubscriptionFromDate());

            dto.setSubscriptionToDate(outletPlan.getSubscriptionToDate());

            dto.setBannerFromDate(outletPlan.getBannerFromDate());

            dto.setBannerToDate(outletPlan.getBannerToDate());

            dto.setBannerSlot(plan.getBannerSlot());

            dto.setBestRestaurantSlot(plan.getBestRestaurantSlot());

            dto.setDealsSlot(plan.getDealsSlot());

            dto.setMainBannerUrl(outletPlan.getMainBannerUrl());

            dto.setBestRestaurantBannerUrl(outletPlan.getBestRestaurantBannerUrl());

            dto.setDealsBannerUrl(outletPlan.getDealsBannerUrl());

            dto.setWhatsappBroadcast(plan.getWhatsappBroadcast());

            dto.setVideoCredits(plan.getVideoCredits());

            dto.setRadiusInKms(plan.getRadiusInKms());

            dto.setPriceModelType(outletPlan.getPriceModelType());

            dto.setOfferAmount(outletPlan.getOfferAmount());

            log.info("{} | SUCCESS | outletId={}", op, outletId);
            return dto;
        } catch (Exception ex) {
            log.error("{} | ERROR fetching designer details | outletId={} | error={}", op, outletId, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public UploadBannerResponseDto uploadBanners(Integer outletSubscriptionPlanId, MultipartFile mainBannerImage, MultipartFile bestRestaurantBannerImage, MultipartFile dealsBannerImage, Integer updatedBy) {
        String op = "UPLOAD_BANNERS_" + UUID.randomUUID();
        try {
            log.info("{} | START | outletSubscriptionPlanId={} | updatedBy={}", op, outletSubscriptionPlanId, updatedBy);

            FmOutletSubscriptionPlan outletSubscription = outletSubscriptionPlanRepository.findById(outletSubscriptionPlanId).orElseThrow(() -> new ResourceNotFoundException("Outlet subscription plan not found"));

            Integer subscriptionPlanId = outletSubscription.getSubscriptionPlanId();

            FmSubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId).orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id : " + subscriptionPlanId));

            if ((mainBannerImage == null || mainBannerImage.isEmpty()) && (bestRestaurantBannerImage == null || bestRestaurantBannerImage.isEmpty()) && (dealsBannerImage == null || dealsBannerImage.isEmpty())) {

                throw new IllegalArgumentException("At least one banner image must be uploaded");
            }

            validateImage(mainBannerImage);
            validateImage(bestRestaurantBannerImage);
            validateImage(dealsBannerImage);

            if (mainBannerImage != null && !mainBannerImage.isEmpty() && (plan.getBannerSlot() == null || plan.getBannerSlot() <= 0)) {

                throw new IllegalArgumentException("Main banner is not enabled for selected subscription plan");
            }

            if (bestRestaurantBannerImage != null && !bestRestaurantBannerImage.isEmpty() && (plan.getBestRestaurantSlot() == null || plan.getBestRestaurantSlot() <= 0)) {

                throw new IllegalArgumentException("Best restaurant banner is not enabled for selected subscription plan");
            }

            if (dealsBannerImage != null && !dealsBannerImage.isEmpty() && (plan.getDealsSlot() == null || plan.getDealsSlot() <= 0)) {

                throw new IllegalArgumentException("Deals banner is not enabled for selected subscription plan");
            }

            // MAIN BANNER

            if (mainBannerImage != null && !mainBannerImage.isEmpty()) {

                if (outletSubscription.getMainBannerUrl() != null) {

                    log.info("{} | MAIN_BANNER_UPDATE | oldUrl={}", op, outletSubscription.getMainBannerUrl());

                } else {

                    log.info("{} | MAIN_BANNER_CREATE", op);
                }

                String mainBannerUrl = s3ImageService.uploadBanner(mainBannerImage, outletSubscription.getOutletId(), plan.getSubscriptionPlanId(), "main-banner", plan.getBannerSlot());
                outletSubscription.setMainBannerUrl(mainBannerUrl);

                log.info("{} | MAIN_BANNER_UPLOADED | url={}", op, mainBannerUrl);
            }

            // BEST RESTAURANT BANNER

            if (bestRestaurantBannerImage != null && !bestRestaurantBannerImage.isEmpty()) {

                if (outletSubscription.getBestRestaurantBannerUrl() != null) {

                    log.info("{} | BEST_RESTAURANT_BANNER_UPDATE | oldUrl={}", op, outletSubscription.getBestRestaurantBannerUrl());

                } else {

                    log.info("{} | BEST_RESTAURANT_BANNER_CREATE", op);
                }

                String bestRestaurantBannerUrl = s3ImageService.uploadBanner(bestRestaurantBannerImage, outletSubscription.getOutletId(), plan.getSubscriptionPlanId(), "best-restaurant-banner", plan.getBestRestaurantSlot());

                outletSubscription.setBestRestaurantBannerUrl(bestRestaurantBannerUrl);

                log.info("{} | BEST_RESTAURANT_BANNER_UPLOADED | url={}", op, bestRestaurantBannerUrl);
            }

            // DEALS BANNER

            if (dealsBannerImage != null && !dealsBannerImage.isEmpty()) {

                if (outletSubscription.getDealsBannerUrl() != null) {

                    log.info("{} | DEALS_BANNER_UPDATE | oldUrl={}", op, outletSubscription.getDealsBannerUrl());

                } else {

                    log.info("{} | DEALS_BANNER_CREATE", op);
                }

                String dealsBannerUrl = s3ImageService.uploadBanner(dealsBannerImage, outletSubscription.getOutletId(), plan.getSubscriptionPlanId(), "deals-banner", plan.getDealsSlot());
                outletSubscription.setDealsBannerUrl(dealsBannerUrl);

                log.info("{} | DEALS_BANNER_UPLOADED | url={}", op, dealsBannerUrl);
            }

            outletSubscription.setUpdatedAt(LocalDateTime.now());

            outletSubscription.setUpdatedBy(updatedBy);

            FmOutletSubscriptionPlan savedOutletSubscription = outletSubscriptionPlanRepository.saveAndFlush(outletSubscription);

            UploadBannerResponseDto response = new UploadBannerResponseDto();

            response.setOutletSubscriptionPlanId(savedOutletSubscription.getOutletSubscriptionPlanId());

            response.setMainBannerUrl(savedOutletSubscription.getMainBannerUrl());

            response.setBestRestaurantBannerUrl(savedOutletSubscription.getBestRestaurantBannerUrl());

            response.setDealsBannerUrl(savedOutletSubscription.getDealsBannerUrl());

            log.info("{} | SUCCESS | outletSubscriptionPlanId={}", op, outletSubscriptionPlanId);
            return response;
        } catch (IOException ex) {
            log.error("{} | S3_UPLOAD_FAILED | outletSubscriptionPlanId={} | error={}", op, outletSubscriptionPlanId, ex.getMessage(), ex);
            throw new BannerUploadException("Failed to upload banner images", ex);
        }
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();

        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg") || contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/webp"))) {
            log.warn("validateImage failed | unsupported contentType={}", contentType);
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP images are allowed");
        }

        long maxSize = 5 * 1024 * 1024; // 5 MB

        if (file.getSize() > maxSize) {
            log.warn("validateImage failed | fileSize={} > maxSize={}", file.getSize(), maxSize);
            throw new IllegalArgumentException("Image size must not exceed 5 MB");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutletSubscriptionPlanResponseDto> getAllSubscriptions() {

        log.info("GET_ALL_SUBSCRIPTIONS_START");

        List<FmOutletSubscriptionPlan> subscriptions = outletSubscriptionPlanRepository.findAll();

        List<OutletSubscriptionPlanResponseDto> response = subscriptions.stream().map(OutletSubscriptionPlanMapper::toDto).toList();

        log.info("GET_ALL_SUBSCRIPTIONS_SUCCESS | count={}", response.size());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OutletSubscriptionStatusResponseDto getSubscriptionStatus(Integer outletId) {

        FmOutletSubscriptionPlan outletPlan = outletSubscriptionPlanRepository.findByOutletId(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet subscription not found"));

        FmSubscriptionPlan plan = subscriptionPlanRepository.findById(outletPlan.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        FmOutlet outlet = outletRepository.findById(outletId).orElseThrow(() -> new ResourceNotFoundException("Outlet not found"));

        LocalDate today = LocalDate.now();

        String status;

        if (today.isBefore(outletPlan.getSubscriptionFromDate())) {

            status = "UPCOMING";

        } else if (today.isAfter(outletPlan.getSubscriptionToDate())) {

            status = "EXPIRED";

        } else {

            status = "ACTIVE";
        }

        OutletSubscriptionStatusResponseDto dto = new OutletSubscriptionStatusResponseDto();

        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());

        dto.setSubscriptionPlanId(plan.getSubscriptionPlanId());

        dto.setPlanName(plan.getPlanName());

        dto.setSubscriptionStatus(status);

        dto.setSubscriptionFromDate(outletPlan.getSubscriptionFromDate());

        dto.setSubscriptionToDate(outletPlan.getSubscriptionToDate());

        dto.setBannerFromDate(outletPlan.getBannerFromDate());

        dto.setBannerToDate(outletPlan.getBannerToDate());

        dto.setBannerSlot(plan.getBannerSlot());

        dto.setBestRestaurantSlot(plan.getBestRestaurantSlot());

        dto.setDealsSlot(plan.getDealsSlot());

        dto.setMainBannerUrl(outletPlan.getMainBannerUrl());

        dto.setBestRestaurantBannerUrl(outletPlan.getBestRestaurantBannerUrl());

        dto.setDealsBannerUrl(outletPlan.getDealsBannerUrl());

        dto.setPriceModelType(outletPlan.getPriceModelType());

        return dto;
    }
    @Override
    @Transactional(readOnly = true)
    public List<ActiveBannerProjection> getActiveBanners() {

        return outletSubscriptionPlanRepository.findActiveBanners();
    }
}