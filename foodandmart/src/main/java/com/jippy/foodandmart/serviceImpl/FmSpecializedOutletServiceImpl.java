package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmNearbyOutletResponseDto;
import com.jippy.foodandmart.dto.FmOutletDto;
import com.jippy.foodandmart.dto.FmPublicNearbyOutletResponseDto;
import com.jippy.foodandmart.mapper.FmPublicNearbyOutletMapper;
import com.jippy.foodandmart.service.FmSpecializedOutletService;
import com.jippy.foodandmart.projections.FmOutletProjection;
import com.jippy.foodandmart.repository.FmSpecializedOutletRepository;
import com.jippy.foodandmart.projections.FmNearbyOutletProjection;
import com.jippy.foodandmart.projections.FmPublicNearbyOutletProjection;
import com.jippy.foodandmart.constants.FmAppConstants;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmSpecializedOutletServiceImpl implements FmSpecializedOutletService {

    private final FmSpecializedOutletRepository repository;

    @Override
    public FmNearbyOutletResponseDto fetchSpecializedOutletsByAreaId(Integer areaId) {

        log.info("[FM Service] Fetching specialized outlets for areaId={}", areaId);

        if (areaId == null || areaId <= 0) {
            log.warn("[FM Service] Invalid areaId: {}", areaId);
            throw new IllegalArgumentException("areaId must be greater than 0");
        }

        List<FmOutletProjection> projections = repository.fetchSpecializedOutletsByAreaId(areaId);

        log.info("[FM Service] DB returned {} outlets", projections.size());

        List<FmOutletDto> outlets = projections.stream()
                .map(data -> {

                    log.info("[FM Service] Mapping outletId={}, outletName={}",
                            data.getOutletId(), data.getOutletName());

                    FmOutletDto dto = new FmOutletDto();
                    dto.setOutletId(data.getOutletId());
                    dto.setOutletName(data.getOutletName());

                    return dto;
                })
                .toList();

        FmNearbyOutletResponseDto response = new FmNearbyOutletResponseDto();
        response.setAreaId(areaId);
        response.setTotalOutlets(outlets.size());
        response.setOutlets(outlets);

        log.info("[FM Service] Response prepared successfully for areaId={}", areaId);

        return response;
    }

//    @Override
//    public FmNearbyOutletResponseDto fetchNearbySpecializedOutlets(Double latitude, Double longitude) {
//
//        List<FmNearbyOutletProjection> projections = repository.fetchNearbySpecializedOutlets(latitude, longitude);
//
//        List<FmOutletDto> outlets = projections.stream().map(data -> {
//
//            FmOutletDto dto = new FmOutletDto();
//
//            dto.setOutletId(data.getOutletId());
//
//            dto.setOutletName(data.getOutletName());
//
//            dto.setDistanceKm(data.getDistanceInKm());
//
//            return dto;
//
//        }).toList();
//
//        FmNearbyOutletResponseDto response = new FmNearbyOutletResponseDto();
//
//        response.setTotalOutlets(outlets.size());
//
//        response.setOutlets(outlets);
//
//        return response;
//    }
@Override
    public FmNearbyOutletResponseDto
fetchNearbySpecializedOutlets(
        Double latitude,
        Double longitude) {

    log.info(
            "[FM Service] Fetching nearby specialized outlets for latitude={} longitude={}",
            latitude,
            longitude);

    List<FmNearbyOutletProjection> projections =
            repository.fetchNearbySpecializedOutlets(
                    latitude,
                    longitude,
                    FmAppConstants.NEARBY_OUTLET_RADIUS_METERS);

    log.info(
            "[FM Service] DB returned {} nearby outlets",
            projections.size());

    List<FmOutletDto> outlets =
            projections.stream()
                    .map(data -> {

                        FmOutletDto dto = new FmOutletDto();

                        dto.setOutletId(data.getOutletId());
                        dto.setOutletName(data.getOutletName());
                        dto.setMerchantId(data.getMerchantId());
                        dto.setCuisineType(data.getCuisineType());
                        dto.setOutletPhone(data.getOutletPhone());
                        dto.setRadius(data.getRadius());
                        dto.setDistanceKm(data.getDistanceInKm());

                        return dto;

                    }).toList();

    FmNearbyOutletResponseDto response =
            new FmNearbyOutletResponseDto();

    response.setTotalOutlets(
            outlets.size());

    response.setOutlets(
            outlets);

    log.info(
            "[FM Service] Nearby outlet response prepared successfully");

    return response;
}

    @Override
    public FmPublicNearbyOutletResponseDto fetchPublicNearbySpecializedOutlets(
            Double latitude,
            Double longitude,
            Integer areaId) {

        log.info(
                "[FM Service] Fetching public nearby specialized outlets for latitude={} longitude={} areaId={}",
                latitude,
                longitude,
                areaId);

        List<FmPublicNearbyOutletProjection> projections =
                repository.fetchPublicNearbySpecializedOutlets(
                        latitude,
                        longitude,
                        FmAppConstants.NEARBY_OUTLET_RADIUS_METERS);

        log.info(
                "[FM Service] DB returned {} public nearby outlets",
                projections.size());

        FmPublicNearbyOutletResponseDto response = new FmPublicNearbyOutletResponseDto();
        response.setAreaId(areaId);
        response.setTotalOutlets(projections.size());
        response.setOutlets(FmPublicNearbyOutletMapper.toDtoList(projections));

        log.info("[FM Service] Public nearby outlet response prepared successfully");

        return response;
    }
}
