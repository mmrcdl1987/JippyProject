package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.entity.BannerSlotDay;
import org.springframework.stereotype.Component;

@Component
public class BannerSlotDayMapper {

    public BannerSlotDayResponseDto toResponseDto(BannerSlotDay entity) {

        BannerSlotDayResponseDto dto = new BannerSlotDayResponseDto();

        dto.setBannerSlotDaysId(entity.getBannerSlotDaysId());
        dto.setSlotStartDate(entity.getSlotStartDate());
        dto.setSlotEndDate(entity.getSlotEndDate());

        return dto;
    }

}