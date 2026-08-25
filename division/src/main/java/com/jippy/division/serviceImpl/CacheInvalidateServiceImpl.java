package com.jippy.division.serviceImpl;

import com.jippy.division.constants.DivAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidateServiceImpl {

    private final StringRedisTemplate redisTemplate;

    public void invalidateCache(Integer outletId) {

        String merchantTypeRedisKey = String.format("menu:outlet:%d:type:%s", outletId, DivAppConstants.TYPE_MERCHANT);
        Boolean merchantTypeCacheInvalidated = redisTemplate.delete(merchantTypeRedisKey);

        String customerTypeRedisKey = String.format("menu:outlet:%d:type:%s", outletId,DivAppConstants.TYPE_CUSTOMER);
        Boolean customerTypeCacheInvalidated = redisTemplate.delete(customerTypeRedisKey);

        log.info("Outlet[{}] cache invalidated",outletId);
    }

}
