package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.dto.FmProductPriceSettingsResponseDto;
import com.jippy.foodandmart.entity.FmProductPriceSettings;
import com.jippy.foodandmart.exception.PriceSettingsException;
import com.jippy.foodandmart.exception.PriceSettingsNotFoundException;
import com.jippy.foodandmart.mapper.FmProductPriceSettingsMapper;
import com.jippy.foodandmart.repository.FmProductPriceSettingsRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.service.IFmProductPriceSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmProductPriceSettingsServiceImpl implements IFmProductPriceSettingsService {

    private final FmProductPriceSettingsRepository priceSettingsRepository;
    private final FmProductPriceSettingsMapper priceSettingsMapper;
    private final FmProductRepository productRepository;

    @Override
    @Transactional
    public FmProductPriceSettingsResponseDto create(FmProductPriceSettingsRequestDto request, Integer userId) {

        if (request == null) {
            throw new PriceSettingsException("Product price settings request cannot be null");
        }

        log.info("Creating product price setting | outletId={} | productId={} | variantId={} | start={} | end={}", request.getOutletId(), request.getProductId(), request.getProductVariantId(), request.getStartDateTime(), request.getEndDateTime());

        validateRequest(request);

        validateProductAndVariant(request.getProductId(), request.getOutletId(), request.getProductVariantId());

        validateOverlappingSettings(request.getOutletId(), request.getProductId(), request.getProductVariantId(), request.getStartDateTime(), request.getEndDateTime(), null);

        LocalDateTime now = LocalDateTime.now();

        FmProductPriceSettings entity = priceSettingsMapper.toEntity(request, userId, now);

        FmProductPriceSettings savedEntity = priceSettingsRepository.save(entity);

        log.info("Product price setting created successfully | settingId={}", savedEntity.getProductPriceSettingsId());

        return priceSettingsMapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public FmProductPriceSettingsResponseDto getById(Integer id) {

        log.info("Fetching product price setting | settingId={}", id);

        validateId(id);

        FmProductPriceSettings entity = findById(id);

        return priceSettingsMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FmProductPriceSettingsResponseDto> getAll(int page, int size) {

        if (page < 0) {
            throw new PriceSettingsException("Page number cannot be negative");
        }

        if (size <= 0 || size > 100) {
            throw new PriceSettingsException("Page size must be between 1 and 100");
        }

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("productPriceSettingsId"));

        Pageable pageable = PageRequest.of(page, size, sort);

        log.debug("Fetching product price settings | page={} | size={}", page, size);

        Page<FmProductPriceSettings> entityPage = priceSettingsRepository.findAll(pageable);

        log.debug("Product price settings fetched | page={} | records={}", page, entityPage.getNumberOfElements());

        return entityPage.map(priceSettingsMapper::toDto);
    }

    @Override
    @Transactional
    public FmProductPriceSettingsResponseDto update(Integer id, FmProductPriceSettingsRequestDto request, Integer userId) {

        log.info("Updating product price setting | settingId={}", id);

        validateId(id);

        if (request == null) {
            throw new PriceSettingsException("Product price settings request cannot be null");
        }

        /*
         * Fetch existing setting first.
         *
         * outletId, productId and productVariantId are immutable
         * during update, therefore use values from the existing entity.
         */
        FmProductPriceSettings entity = findById(id);

        validateRequest(request);

        /*
         * No product/variant validation here.
         *
         * These values cannot be changed by updateEntity().
         */
        validateOverlappingSettings(entity.getOutletId(), entity.getProductId(), entity.getProductVariantId(), request.getStartDateTime(), request.getEndDateTime(), id);

        priceSettingsMapper.updateEntity(entity, request, userId, LocalDateTime.now());

        log.info("Product price setting updated successfully | settingId={}", entity.getProductPriceSettingsId());

        return priceSettingsMapper.toDto(entity);
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        log.info("Deleting product price setting | settingId={}", id);

        validateId(id);

        if (!priceSettingsRepository.existsById(id)) {
            log.warn("Product price setting not found | settingId={}", id);

            throw new PriceSettingsNotFoundException("Product price setting not found with id: " + id);
        }

        priceSettingsRepository.deleteById(id);

        log.info("Product price setting deleted successfully | settingId={}", id);
    }

    private FmProductPriceSettings findById(Integer id) {

        return priceSettingsRepository.findById(id).orElseThrow(() -> {

            log.warn("Product price setting not found | settingId={}", id);

            return new PriceSettingsNotFoundException("Product price setting not found with id: " + id);
        });
    }

    private void validateId(Integer id) {

        if (id == null || id <= 0) {

            log.warn("Invalid product price setting id | settingId={}", id);

            throw new PriceSettingsException("Invalid product price setting id");
        }
    }

    private void validateRequest(FmProductPriceSettingsRequestDto request) {

        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {

            throw new PriceSettingsException("Start date time and end date time are required");
        }

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {

            log.warn("Invalid price setting date range | start={} | end={}", request.getStartDateTime(), request.getEndDateTime());

            throw new PriceSettingsException("End date time must be after start date time");
        }
    }

    private void validateOverlappingSettings(Integer outletId, Integer productId, Integer productVariantId, LocalDateTime startDateTime, LocalDateTime endDateTime, Integer currentSettingId) {

        boolean overlapExists = priceSettingsRepository.existsOverlappingPriceSetting(outletId, productId, productVariantId, startDateTime, endDateTime, currentSettingId);

        if (overlapExists) {

            log.warn("Overlapping price setting detected | outletId={} | productId={} | variantId={} | start={} | end={}", outletId, productId, productVariantId, startDateTime, endDateTime);

            throw new PriceSettingsException("Another price setting already exists for the same product/variant and time period");
        }
    }

    private void validateProductAndVariant(Integer productId, Integer outletId, Integer productVariantId) {

        boolean valid = productRepository.existsActiveProductAndVariantInOutlet(productId, outletId, productVariantId);

        if (!valid) {

            log.warn("Invalid product/variant for outlet | productId={} | outletId={} | variantId={}", productId, outletId, productVariantId);

            throw new PriceSettingsException("Product or product variant does not belong to the selected outlet");
        }
    }
}