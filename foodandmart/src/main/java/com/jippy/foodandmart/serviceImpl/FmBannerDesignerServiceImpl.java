package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmBannerDesignerResponseDto;
import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.repository.IFmOutletSubscriptionPlanRepository;
import com.jippy.foodandmart.service.IFmBannerDesignerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmBannerDesignerServiceImpl
        implements IFmBannerDesignerService {

    private final IFmOutletSubscriptionPlanRepository repository;

    @Override
    public List<FmBannerDesignerResponseDto> getAllBannerDesigners() {

        log.info("Fetching Banner Designer records from database.");

        List<FmOutletSubscriptionPlan> banners = repository.findAll();

        log.info("Total Banner Records Found : {}", banners.size());

        return banners.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private FmBannerDesignerResponseDto convertToDto(
            FmOutletSubscriptionPlan entity) {

        FmBannerDesignerResponseDto dto =
                new FmBannerDesignerResponseDto();

        dto.setOutletSubscriptionPlanId(entity.getOutletSubscriptionPlanId());
        dto.setOutletId(entity.getOutletId());
        dto.setSubscriptionPlanId(entity.getSubscriptionPlanId());
        dto.setMainBannerUrl(entity.getMainBannerUrl());
        dto.setBestRestaurantBannerUrl(entity.getBestRestaurantBannerUrl());
        dto.setDealsBannerUrl(entity.getDealsBannerUrl());
        dto.setPriceModelType(entity.getPriceModelType());
        dto.setOfferAmount(entity.getOfferAmount());


        return dto;
    }

    private String calculateStatus(
            LocalDate bannerFromDate,
            LocalDate bannerToDate) {

        if (bannerFromDate == null ||
                bannerToDate == null) {

            return "INACTIVE";
        }

        LocalDate today = LocalDate.now();

        if ((today.isEqual(bannerFromDate)
                || today.isAfter(bannerFromDate))
                &&
                (today.isEqual(bannerToDate)
                        || today.isBefore(bannerToDate))) {

            return "ACTIVE";
        }

        return "INACTIVE";
    }

}