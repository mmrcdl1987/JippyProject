package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.entity.FmArea;
import com.jippy.foodandmart.repository.FmAreaRepository;
import com.jippy.foodandmart.service.IFmAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmAreaServiceImpl implements IFmAreaService {

    private final FmAreaRepository areaRepository;

    @Override
    public List<FmAreaDto> getAllAreas() {

        log.info("GET_ALL_AREAS_SERVICE_START");

        List<FmArea> areas = areaRepository.findAll();

        log.info("AREAS_FETCHED_SUCCESSFULLY | count={}", areas.size());

        return areas.stream()
                .map(area -> {
                    FmAreaDto dto = new FmAreaDto();

                    dto.setAreaId(area.getAreaId());
                    dto.setAreaName(area.getAreaName());
                    dto.setCityId(area.getCityId());
                    dto.setCreatedAt(area.getCreatedAt());
                    dto.setCreatedBy(area.getCreatedBy());
                    dto.setUpdatedAt(area.getUpdatedAt());
                    dto.setUpdatedBy(area.getUpdatedBy());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}