package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.AreaBannerCacheDto;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.service.CustomerBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBannerServiceImpl implements CustomerBannerService {

    private final RedisTemplate<String, AreaBannerCacheDto> redisTemplate;

    @Override
    public AreaBannerCacheDto getCustomerBanners(Integer areaId) {

        log.info("Fetching banner cache for areaId={}", areaId);

        String redisKey = "AREA_" + areaId;

        AreaBannerCacheDto response =
                redisTemplate.opsForValue().get(redisKey);

        if (response == null) {
            throw new ResourceNotFoundException(
                    "No active banners found for area : " + areaId);
        }

        return response;
    }
}