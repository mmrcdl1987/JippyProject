package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmProductVariantOptionRequestDto;
import com.jippy.foodandmart.dto.FmProductVariantOptionResponseDto;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductVariantGroupValue;
import com.jippy.foodandmart.entity.FmProductVariantOption;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmProductVariantOptionMapper;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.FmProductVariantGroupValueRepository;
import com.jippy.foodandmart.repository.FmProductVariantOptionRepository;
import com.jippy.foodandmart.service.IFmProductVariantOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FmProductVariantOptionServiceImpl
        implements IFmProductVariantOptionService {

    private static final String MAIN = "MAIN";
    private static final String ADD = "ADD";

    private final FmProductRepository productRepository;
    private final FmProductVariantGroupValueRepository productVariantGroupValueRepository;
    private final FmProductVariantOptionRepository productVariantOptionRepository;
    private final CacheInvalidateServiceImpl cacheInvalidateService;

    @Override
    public FmProductVariantOptionResponseDto saveProductVariantOption(
            Integer productId,
            FmProductVariantOptionRequestDto request) {

        log.info("Save Product Variant Option request. ProductId={}, VariantValueId={}",
                productId,
                request.getProductVariantGroupValuesId());

        validateProductExists(productId);

        validateVariantValueExists(request.getProductVariantGroupValuesId());

        validateRequest(productId, request);

        if (request.getProductVariantOptionsId() == null) {
            return createProductVariantOption(productId, request);
        }

        return updateProductVariantOption(productId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FmProductVariantOptionResponseDto> getProductVariantOptions(
            Integer productId) {

        log.info("Fetching Product Variant Options. ProductId={}", productId);

        validateProductExists(productId);

        List<FmProductVariantOption> options =
                productVariantOptionRepository
                        .findByProductIdAndIsActiveTrueOrderByProductVariantOptionsIdAsc(
                                productId);

        log.info("Fetched {} Variant Options for ProductId={}",
                options.size(),
                productId);

        return options.stream()
                .map(FmProductVariantOptionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FmProductVariantOptionResponseDto getProductVariantOptionById(
            Integer productId,
            Integer productVariantOptionId) {

        log.info("Fetching Product Variant Option. ProductId={}, OptionId={}",
                productId,
                productVariantOptionId);

        validateProductExists(productId);

        FmProductVariantOption entity =
                productVariantOptionRepository
                        .findByProductVariantOptionsIdAndProductIdAndIsActiveTrue(
                                productVariantOptionId,
                                productId)
                        .orElseThrow(() -> {

                            log.warn("Product Variant Option not found. ProductId={}, OptionId={}",
                                    productId,
                                    productVariantOptionId);

                            return new ResourceNotFoundException(
                                    "Product Variant Option not found with id : "
                                            + productVariantOptionId);
                        });

        log.info("Product Variant Option found. OptionId={}",
                entity.getProductVariantOptionsId());

        return FmProductVariantOptionMapper.toResponseDto(entity);
    }

    @Override
    public void deleteProductVariantOption(
            Integer productId,
            Integer productVariantOptionId) {

        log.info("Deleting Product Variant Option. ProductId={}, OptionId={}",
                productId,
                productVariantOptionId);

        validateProductExists(productId);

        FmProductVariantOption entity =
                productVariantOptionRepository
                        .findByProductVariantOptionsIdAndProductIdAndIsActiveTrue(
                                productVariantOptionId,
                                productId)
                        .orElseThrow(() -> {

                            log.warn("Product Variant Option not found. ProductId={}, OptionId={}",
                                    productId,
                                    productVariantOptionId);

                            return new ResourceNotFoundException(
                                    "Product Variant Option not found with id : "
                                            + productVariantOptionId);
                        });

        entity.setIsActive(Boolean.FALSE);

        productVariantOptionRepository.save(entity);

        log.info("Product Variant Option deleted successfully. OptionId={}, ProductId={}",
                entity.getProductVariantOptionsId(),
                productId);
    }
    private FmProductVariantOptionResponseDto createProductVariantOption(
            Integer productId,
            FmProductVariantOptionRequestDto request) {

        FmProductVariantOption entity =
                FmProductVariantOptionMapper.toEntity(productId, request);

        entity = productVariantOptionRepository.save(entity);

        log.info("Product Variant Option created successfully. OptionId={}, ProductId={}, VariantValueId={}",
                entity.getProductVariantOptionsId(),
                productId,
                entity.getProductVariantGroupValuesId());

        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);

        return FmProductVariantOptionMapper.toResponseDto(entity);
    }
    private FmProductVariantOptionResponseDto updateProductVariantOption(
            Integer productId,
            FmProductVariantOptionRequestDto request) {

        log.info("Updating Product Variant Option. OptionId={}",
                request.getProductVariantOptionsId());

        FmProductVariantOption entity =
                productVariantOptionRepository
                        .findByProductVariantOptionsIdAndProductIdAndIsActiveTrue(
                                request.getProductVariantOptionsId(),
                                productId)
                        .orElseThrow(() -> {

                            log.warn("Product Variant Option not found. OptionId={}",
                                    request.getProductVariantOptionsId());

                            return new ResourceNotFoundException(
                                    "Product Variant Option not found with id : "
                                            + request.getProductVariantOptionsId());
                        });

        entity.setProductVariantGroupValuesId(
                request.getProductVariantGroupValuesId());

        entity.setPriceType(
                request.getPriceType().trim().toUpperCase());

        entity.setVariantPrice(
                request.getVariantPrice());

        entity = productVariantOptionRepository.save(entity);

        log.info("Product Variant Option updated successfully. OptionId={}",
                entity.getProductVariantOptionsId());

        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);

        return FmProductVariantOptionMapper.toResponseDto(entity);
    }
    private FmProduct validateProductExists(Integer productId) {

        return productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn("Product not found. ProductId={}", productId);

                    return new ResourceNotFoundException(
                            "Product not found with id : " + productId);
                });
    }
    private FmProductVariantGroupValue validateVariantValueExists(
            Integer productVariantGroupValuesId) {

        return productVariantGroupValueRepository
                .findByProductVariantGroupValuesIdAndIsActiveTrue(
                        productVariantGroupValuesId)
                .orElseThrow(() -> {

                    log.warn("Product Variant Value not found. ValueId={}",
                            productVariantGroupValuesId);

                    return new ResourceNotFoundException(
                            "Product Variant Value not found with id : "
                                    + productVariantGroupValuesId);
                });
    }
    private void validateRequest(
            Integer productId,
            FmProductVariantOptionRequestDto request) {

        validatePriceType(request.getPriceType());

        if (request.getVariantPrice() == null
                || request.getVariantPrice().signum() < 0) {

            log.error("Invalid Variant Price : {}", request.getVariantPrice());

            throw new IllegalArgumentException(
                    "Variant Price must be greater than or equal to zero.");
        }

        if (request.getProductVariantOptionsId() == null) {

            if (productVariantOptionRepository
                    .existsByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(
                            productId,
                            request.getProductVariantGroupValuesId())) {

                log.warn("Duplicate Product Variant Option. ProductId={}, VariantValueId={}",
                        productId,
                        request.getProductVariantGroupValuesId());

                throw new DuplicateResourceException(
                        "Variant already mapped to this product.");
            }

        } else {

            if (productVariantOptionRepository
                    .existsByProductIdAndProductVariantGroupValuesIdAndProductVariantOptionsIdNotAndIsActiveTrue(
                            productId,
                            request.getProductVariantGroupValuesId(),
                            request.getProductVariantOptionsId())) {

                log.warn("Duplicate Product Variant Option. ProductId={}, VariantValueId={}",
                        productId,
                        request.getProductVariantGroupValuesId());

                throw new DuplicateResourceException(
                        "Variant already mapped to this product.");
            }
        }
    }
    private void validatePriceType(String priceType) {

        if (priceType == null || priceType.isBlank()) {

            log.error("Price Type is missing.");

            throw new IllegalArgumentException(
                    "Price Type is required.");
        }

        String value = priceType.trim().toUpperCase();

        if (!MAIN.equals(value) && !ADD.equals(value)) {

            log.error("Invalid Price Type : {}", priceType);

            throw new IllegalArgumentException(
                    "Price Type must be MAIN or ADD.");
        }
    }
}