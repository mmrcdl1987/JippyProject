package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// ...existing imports...
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fm/outlet-subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class OutletSubscriptionPlanController {

    private final OutletSubscriptionPlanService outletSubscriptionPlanService;



    private String startOperation(String prefix) {
        // generate a short operation id for log correlation within this request
        return prefix + "_" + UUID.randomUUID();
    }

    private void endOperation() {
        // no-op: we don't use MDC, keep logs explicit per statement
    }

    @PostMapping
    public FmApiResponse<OutletSubscriptionPlanResponseDto> subscribeOutlet(@Valid @RequestBody OutletSubscriptionPlanRequestDto request) {
        String op = startOperation("SUBSCRIBE_OUTLET");
        log.info("API_START | {} | outletId={}", op, request.getOutletId());
        try {
            OutletSubscriptionPlanResponseDto response = outletSubscriptionPlanService.subscribeOutlet(request);
            log.info("API_SUCCESS | {} | outletId={} | outletSubscriptionPlanId={}", op, request.getOutletId(), response == null ? "n/a" : response.getOutletSubscriptionPlanId());
            return FmApiResponse.success("Outlet subscribed successfully", response);
        } catch (Exception ex) {
            log.error("API_ERROR | {} | outletId={} | error={}", op, request.getOutletId(), ex.getMessage(), ex);
            throw ex;
        } finally {
            endOperation();
        }
    }

    @GetMapping("/designer/{outletId}")
    public FmApiResponse<OutletBannerDesignerResponseDto> getDesignerDetails(@PathVariable Integer outletId) {
        String op = startOperation("DESIGNER_DETAILS");
        log.info("API_START | {} | outletId={}", op, outletId);
        try {
            OutletBannerDesignerResponseDto response = outletSubscriptionPlanService.getDesignerDetails(outletId);
            log.info("API_SUCCESS | {} | outletId={} | designerFound={}", op, outletId, response != null);
            return FmApiResponse.success("Designer details fetched successfully", response);
        } catch (Exception ex) {
            log.error("API_ERROR | {} | outletId={} | error={}", op, outletId, ex.getMessage(), ex);
            throw ex;
        } finally {
            endOperation();
        }
    }

    @PostMapping(value = "/upload-banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FmApiResponse<UploadBannerResponseDto> uploadBanners(

            @RequestParam(required = true) Integer outletSubscriptionPlanId,

            @RequestParam(required = false) MultipartFile mainBannerImage,

            @RequestParam(required = false) MultipartFile bestRestaurantBannerImage,

            @RequestParam(required = false) MultipartFile dealsBannerImage,

            @RequestParam(required = true) Integer updatedBy) {

        String op = startOperation("UPLOAD_BANNERS");
        log.info("API_START | {} | outletSubscriptionPlanId={} | updatedBy={}", op, outletSubscriptionPlanId, updatedBy);

        try {
            if (mainBannerImage != null) {
                log.debug("{} | mainBannerImage | name={} | size={} bytes", op, mainBannerImage.getOriginalFilename(), mainBannerImage.getSize());
            }
            if (bestRestaurantBannerImage != null) {
                log.debug("{} | bestRestaurantBannerImage | name={} | size={} bytes", op, bestRestaurantBannerImage.getOriginalFilename(), bestRestaurantBannerImage.getSize());
            }
            if (dealsBannerImage != null) {
                log.debug("{} | dealsBannerImage | name={} | size={} bytes", op, dealsBannerImage.getOriginalFilename(), dealsBannerImage.getSize());
            }

            UploadBannerResponseDto response = outletSubscriptionPlanService.uploadBanners(outletSubscriptionPlanId, mainBannerImage, bestRestaurantBannerImage, dealsBannerImage, updatedBy);

            log.info("API_SUCCESS | {} | outletSubscriptionPlanId={} | uploadedUrls=[main={},best={},deals={}]", op, outletSubscriptionPlanId,
                    response == null ? "n/a" : response.getMainBannerUrl(),
                    response == null ? "n/a" : response.getBestRestaurantBannerUrl(),
                    response == null ? "n/a" : response.getDealsBannerUrl());

            return FmApiResponse.success("Banner images uploaded successfully", response);
        } catch (Exception ex) {
            log.error("API_ERROR | {} | outletSubscriptionPlanId={} | error={}", op, outletSubscriptionPlanId, ex.getMessage(), ex);
            throw ex;
        } finally {
            endOperation();
        }
    }

    @GetMapping
    public FmApiResponse<List<OutletSubscriptionPlanResponseDto>> getAllSubscriptions() {
        String op = startOperation("GET_ALL_SUBSCRIPTIONS");
        log.info("API_START | {}", op);
        try {
            List<OutletSubscriptionPlanResponseDto> list = outletSubscriptionPlanService.getAllSubscriptions();
            log.info("API_SUCCESS | {} | count={}", op, list == null ? 0 : list.size());
            return FmApiResponse.success("Subscriptions fetched successfully", list);
        } catch (Exception ex) {
            log.error("API_ERROR | {} | error={}", op, ex.getMessage(), ex);
            throw ex;
        } finally {
            endOperation();
        }
    }

    @GetMapping("/status/{outletId}")
    public FmApiResponse<OutletSubscriptionStatusResponseDto> getSubscriptionStatus(@PathVariable Integer outletId) {
        String op = startOperation("GET_SUBSCRIPTION_STATUS");
        log.info("API_START | {} | outletId={}", op, outletId);
        try {
            OutletSubscriptionStatusResponseDto dto = outletSubscriptionPlanService.getSubscriptionStatus(outletId);
            log.info("API_SUCCESS | {} | outletId={} | subscribed={}", op, outletId, dto != null);
            return FmApiResponse.success("Subscription status fetched successfully", dto);
        } catch (Exception ex) {
            log.error("API_ERROR | {} | outletId={} | error={}", op, outletId, ex.getMessage(), ex);
            throw ex;
        } finally {
            endOperation();
        }
    }
}