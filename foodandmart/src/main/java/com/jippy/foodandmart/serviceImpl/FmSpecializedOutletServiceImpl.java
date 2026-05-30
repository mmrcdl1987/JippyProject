package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmNearbyOutletResponseDto;
import com.jippy.foodandmart.dto.FmOutletDto;
import com.jippy.foodandmart.service.FmSpecializedOutletService;
import com.jippy.foodandmart.projections.FmOutletProjection;
import com.jippy.foodandmart.repository.FmSpecializedOutletRepository;
import com.jippy.foodandmart.projections.FmNearbyOutletProjection;
import com.jippy.foodandmart.constants.FmAppConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmSpecializedOutletServiceImpl implements FmSpecializedOutletService {

    private final FmSpecializedOutletRepository repository;

    @Override
    public FmNearbyOutletResponseDto fetchSpecializedOutletsByAreaId(Integer areaId) {

        log.info("[FM Service] Fetching specialized outlets for areaId={}", areaId);

        List<FmOutletProjection> projections = repository.fetchSpecializedOutletsByAreaId(areaId);

        log.info("[FM Service] DB returned {} outlets", projections.size());

        List<FmOutletDto> outlets = projections.stream().map(data -> {

            log.info("[FM Service] Mapping outletId={} outletName={}", data.getOutletId(), data.getOutletName());

            FmOutletDto dto = new FmOutletDto();

            dto.setOutletId(data.getOutletId());

            dto.setOutletName(data.getOutletName());

            return dto;
        }).toList();

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

                        FmOutletDto dto =
                                new FmOutletDto();

                        dto.setOutletId(
                                data.getOutletId());

                        dto.setOutletName(
                                data.getOutletName());

                        dto.setDistanceKm(
                                data.getDistanceInKm());

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
}