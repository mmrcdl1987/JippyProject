package com.jippy.division.serviceImpl;

import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.dto.DivCampaignSlotDto;
import com.jippy.division.dto.DivOutletDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.entity.DivPromotionTime;
import com.jippy.division.feignclients.FMFeignClient;
import com.jippy.division.mapper.DivCampaignMapper;
import com.jippy.division.repositary.DivCouponMappingRepository;
import com.jippy.division.repositary.DivPriceDropMappingRepository;
import com.jippy.division.repositary.DivPromotionDateRepository;
import com.jippy.division.repositary.DivPromotionTimeRepository;
import com.jippy.division.service.IDivCampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivCampaignServiceImpl implements IDivCampaignService {

    private final DivPromotionDateRepository promotionDateRepository;

    private final DivPromotionTimeRepository promotionTimeRepository;

    private final DivCouponMappingRepository mappingRepository;

    private final DivPriceDropMappingRepository priceDropRepository;

    private final FMFeignClient fmClient;

    @Override
    public String createCampaign(DivCampaignRequestDto dto) {

        log.info("Campaign creation started");

        // SLOT VALIDATION

        if (dto.getSlots() == null || dto.getSlots().isEmpty()) {

            throw new RuntimeException("Campaign slots are required");
        }

        // OUTLET VALIDATION

        if (dto.getOutletIds() == null || dto.getOutletIds().isEmpty()) {

            throw new RuntimeException("OutletIds are required");
        }

        // SAVE PROMOTION DATE

        DivPromotionDate promotionDate = DivCampaignMapper.mapToPromotionDateEntity(dto);

        promotionDateRepository.save(promotionDate);

        log.info("Promotion date saved successfully");

        // LOOP SLOTS

        for (DivCampaignSlotDto slot : dto.getSlots()) {

            // SAVE PROMOTION TIME

            DivPromotionTime promotionTime = DivCampaignMapper.mapToPromotionTimeEntity(slot, promotionDate.getPromotionDateId(), dto.getCreatedBy());

            promotionTimeRepository.save(promotionTime);

            log.info("Promotion time saved");

            // LOOP OUTLETS

            for (Integer outletId : dto.getOutletIds()) {

                // =====================================
                // PRODUCTS AVAILABLE
                // =====================================

                if (dto.getProductIds() != null && !dto.getProductIds().isEmpty()) {

                    for (Integer productId : dto.getProductIds()) {

                        // =========================
                        // COUPON FLOW
                        // =========================

                        if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

                            DivCouponMappingOutletProduct mapping = DivCampaignMapper.mapToCouponMappingEntity(dto.getCouponId(), outletId, productId, dto.getAreaId(), promotionTime.getPromotionTimeId(), dto.getCreatedBy());

                            mappingRepository.save(mapping);

                            log.info("Coupon mapping saved");
                        }

                        // =========================
                        // PRICE DROP FLOW
                        // =========================

                        if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

                            DivPriceDropMappingOutletProduct entity = DivCampaignMapper.mapToPriceDropEntity(outletId, productId, dto.getAreaId(), promotionTime.getPromotionTimeId(), dto.getPriceModelId(), dto.getPriceDropValue(), dto.getCreatedBy());

                            priceDropRepository.save(entity);

                            log.info("Price drop saved");
                        }
                    }
                }

                // =====================================
                // PRODUCTS NOT AVAILABLE
                // =====================================

                else {

                    // =========================
                    // COUPON FLOW
                    // =========================

                    if ("COUPON".equalsIgnoreCase(dto.getCampainType())) {

                        DivCouponMappingOutletProduct mapping = DivCampaignMapper.mapToCouponMappingEntity(dto.getCouponId(), outletId, null, dto.getAreaId(), promotionTime.getPromotionTimeId(), dto.getCreatedBy());

                        mappingRepository.save(mapping);

                        log.info("Coupon mapping saved");
                    }

                    // =========================
                    // PRICE DROP FLOW
                    // =========================

                    if ("PRICE_DROP".equalsIgnoreCase(dto.getCampainType())) {

                        DivPriceDropMappingOutletProduct entity = DivCampaignMapper.mapToPriceDropEntity(outletId, null, dto.getAreaId(), promotionTime.getPromotionTimeId(), dto.getPriceModelId(), dto.getPriceDropValue(), dto.getCreatedBy());

                        priceDropRepository.save(entity);

                        log.info("Price drop saved");
                    }
                }
            }
        }

        log.info("Campaign created successfully");

        return "Campaign Created Successfully";
    }

    @Override
    public List<DivOutletDto> getAvailableOutlets(Integer areaId) {

        log.info("Fetching available outlets by areaId={}", areaId);

        List<DivOutletDto> allOutlets = fmClient.getOutletsByAreaId(areaId);

        List<Integer> couponOutlets = mappingRepository.findActiveCouponOutlets();

        List<Integer> priceDropOutlets = priceDropRepository.findActivePriceDropOutlets();

        Set<Integer> blockedOutlets = new HashSet<>();

        blockedOutlets.addAll(couponOutlets);

        blockedOutlets.addAll(priceDropOutlets);

        return allOutlets.stream().filter(outlet -> !blockedOutlets.contains(outlet.getOutletId())).toList();
    }
}