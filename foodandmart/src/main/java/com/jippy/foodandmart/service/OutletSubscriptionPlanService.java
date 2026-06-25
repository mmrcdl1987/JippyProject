package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OutletSubscriptionPlanService {

    OutletSubscriptionPlanResponseDto subscribeOutlet(
            OutletSubscriptionPlanRequestDto request);

    OutletBannerDesignerResponseDto getDesignerDetails(
            Integer outletId);
    UploadBannerResponseDto uploadBanners(
            Integer outletSubscriptionPlanId,
            MultipartFile mainBannerImage,
            MultipartFile bestRestaurantBannerImage,
            MultipartFile dealsBannerImage,
            Integer updatedBy);
    List<OutletSubscriptionPlanResponseDto> getAllSubscriptions();
    public OutletSubscriptionStatusResponseDto getSubscriptionStatus(
            Integer outletId);
}