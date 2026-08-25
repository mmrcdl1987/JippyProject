package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.repository.FmProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidateServiceImpl {

    private final StringRedisTemplate redisTemplate;
    private  final FmProductRepository productRepository;

    public void invalidateCache(Integer outletId) {

        String merchantTypeRedisKey = String.format("menu:outlet:%d:type:%s", outletId, FmAppConstants.TYPE_MERCHANT);
        Boolean merchantTypeCacheInvalidated = redisTemplate.delete(merchantTypeRedisKey);

        String customerTypeRedisKey = String.format("menu:outlet:%d:type:%s", outletId,FmAppConstants.TYPE_CUSTOMER);
        Boolean customerTypeCacheInvalidated = redisTemplate.delete(customerTypeRedisKey);

        log.info("Outlet[{}] cache invalidated",outletId);
    }

    public Integer getOutletIdForProduct(Integer productId) {

        Integer outletId = productRepository.fetchOutletIdForProductId(productId);
        return outletId;
    }


}
