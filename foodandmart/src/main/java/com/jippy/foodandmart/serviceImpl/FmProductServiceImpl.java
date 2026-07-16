package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.mapper.FmProductVariantOptionMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.FmProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmProductServiceImpl implements FmProductService {

    private static final Integer SYSTEM_USER = 1;

    private final FmProductRepository productRepository;

    private final FmProductVariantGroupRepository productVariantGroupRepository;

    private final FmProductVariantGroupValueRepository productVariantGroupValueRepository;

    private final FmProductVariantOptionRepository productVariantOptionRepository;

    private final FmOutletCategoryRepository outletCategoryRepository;

    private final FmMasterProductRepository masterProductRepository;

    private final FmProductAvailableTimingRepository productAvailableTimingRepository;

    private final FmCategoryRepository categoryRepository;


    private final FmDaysOfWeekRepository daysOfWeekRepository;

    @Override
    public FmMapToProductResult mapToProducts(FmMapToProduct request) {

        log.info("Received product mapping request. OutletId={}, CategoryId={}, Products={}",
                request.getOutletId(),
                request.getCategoryId(),
                request.getProducts() == null ? 0 : request.getProducts().size());

        if (request.getProducts() == null || request.getProducts().isEmpty()) {

            throw new IllegalArgumentException("Products are required.");
        }

        if (request.getOutletId() == null) {

            throw new IllegalArgumentException("Outlet Id is required.");
        }

        if (request.getCategoryId() == null) {

            throw new IllegalArgumentException("Category Id is required.");
        }

        /*
         * Find Existing Outlet Category or Create New
         */
        FmOutletCategory outletCategory = outletCategoryRepository
                .findByOutletIdAndCategoryId(
                        request.getOutletId(),
                        request.getCategoryId())
                .orElseGet(() -> {

                    FmOutletCategory entity = new FmOutletCategory();

                    entity.setOutletId(request.getOutletId());
                    entity.setCategoryId(request.getCategoryId());
                    entity.setCreatedBy(SYSTEM_USER);
                    entity.setUpdatedBy(SYSTEM_USER);
                    entity.setIsToggle(true);
                    entity.setIsActive("Y");

                    FmOutletCategory saved = outletCategoryRepository.save(entity);

                    log.info("Created Outlet Category. OutletCategoryId={}",
                            saved.getOutletCategoryId());

                    return saved;
                });

        Integer resolvedOutletCategoryId =
                outletCategory.getOutletCategoryId();

        List<String> savedNames = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();

        for (ProductEntry entry : request.getProducts()) {

            String productName =
                    entry.getProductName() == null
                            ? ""
                            : entry.getProductName().trim();

            if (productName.isBlank()) {

                skippedNames.add("(blank)");
                continue;
            }

            if (productRepository.existsByOutletCategoryIdAndProductNameIgnoreCase(
                    resolvedOutletCategoryId,
                    productName)) {

                skippedNames.add(productName + " (Already Exists)");
                continue;
            }

            String imageLink = null;
            String photos = null;
            String thumbnail = null;

            if (entry.getMasterProductId() == null) {

                throw new IllegalArgumentException("Master Product Id is required.");
            }

            FmMasterProduct masterProduct = masterProductRepository
                    .findById(entry.getMasterProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Master Product not found with id : "
                                    + entry.getMasterProductId()));

            /*
             * Validate that the selected master product belongs
             * to the selected category.
             */
            if (!Objects.equals(masterProduct.getCategoryId(), request.getCategoryId())) {

                throw new IllegalArgumentException(
                        "Selected Master Product does not belong to Category Id : "
                                + request.getCategoryId());
            }

            /*
             * Copy Images
             */
            imageLink = masterProduct.getPhoto();
            photos = masterProduct.getPhotos();
            thumbnail = masterProduct.getThumbnail();

            if (imageLink == null || imageLink.isBlank()) {

                throw new IllegalArgumentException(
                        "Master Product image missing : " + productName);
            }
            boolean hasVariants =
                    Boolean.TRUE.equals(entry.getHasProductVariants());

            FmProduct product = new FmProduct();

            product.setOutletCategoryId(resolvedOutletCategoryId);
            product.setProductName(productName);
            product.setDescription(
                    entry.getDescription() == null
                            ? ""
                            : entry.getDescription());

            product.setIsVeg(
                    entry.getIsVeg() == null
                            ? Boolean.TRUE
                            : entry.getIsVeg());

            product.setHasProductVariants(hasVariants);

            product.setImageLink(imageLink);
            product.setPhotos(photos);
            product.setThumbnail(thumbnail);

            product.setCreatedBy(SYSTEM_USER);
            product.setUpdatedBy(SYSTEM_USER);

            if (hasVariants) {

                product.setMerchantPrice(BigDecimal.ZERO);

            } else {

                BigDecimal requestPrice = entry.getMerchantPrice();

                if (requestPrice != null
                        && requestPrice.compareTo(BigDecimal.ZERO) > 0) {

                    product.setMerchantPrice(requestPrice);

                } else {

                    product.setMerchantPrice(resolvePrice(productName));
                }
            }

            FmProduct savedProduct =
                    productRepository.save(product);

            if (hasVariants) {

                saveProductVariantOptions(
                        savedProduct.getProductId(),
                        entry.getVariantGroups());
            }

            saveTimings(savedProduct.getProductId(), entry);

            savedNames.add(productName);

            log.info("Product saved successfully. ProductId={}, Name={}",
                    savedProduct.getProductId(),
                    savedProduct.getProductName());
        }

        FmMapToProductResult response =
                new FmMapToProductResult();

        response.setSavedCount(savedNames.size());
        response.setSkippedCount(skippedNames.size());
        response.setSavedNames(savedNames);
        response.setSkippedNames(skippedNames);

        return response;
    }
//
//    @Override
//    public FmMasterProductMappingResultDTO mapFromMasterByCategory(Integer outletCategoryId) {
//
//        log.info("Started mapping master products. OutletCategoryId={}", outletCategoryId);
//
//        FmOutletCategory outletCategory = outletCategoryRepository.findById(outletCategoryId).orElseThrow(() -> new ResourceNotFoundException("Outlet Category not found with id : " + outletCategoryId));
//
//        Integer categoryId = outletCategory.getCategoryId();
//
//        FmCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found with id : " + categoryId));
//
//        List<FmMasterProduct> masterProducts = masterProductRepository.findByCategoryIdOrderByMasterProductIdAsc(categoryId);
//
//        log.info("Found {} master products for categoryId={}", masterProducts.size(), categoryId);
//
//        List<String> savedNames = new ArrayList<>();
//        List<String> skippedNames = new ArrayList<>();
//
//        for (FmMasterProduct masterProduct : masterProducts) {
//
//            if (masterProduct.getPublish() == null || masterProduct.getPublish() != 1) {
//
//                skippedNames.add(masterProduct.getMasterProductName());
//
//                continue;
//            }
//
//            String productName = masterProduct.getMasterProductName().trim();
//
//            if (productRepository.existsByOutletCategoryIdAndProductNameIgnoreCase(outletCategoryId, productName)) {
//
//                log.warn("Product already exists. Product={}", productName);
//
//                skippedNames.add(productName + " (Already Exists)");
//
//                continue;
//            }
//
//            if (masterProduct.getPhoto() == null || masterProduct.getPhoto().isBlank()) {
//
//                log.warn("Master product image missing. Product={}", productName);
//
//                skippedNames.add(productName);
//
//                continue;
//            }
//
//            FmProduct product = new FmProduct();
//
//            product.setOutletCategoryId(outletCategoryId);
//            product.setProductName(productName);
//            product.setDescription(masterProduct.getDescription() == null ? "" : masterProduct.getDescription());
//
//            product.setMerchantPrice(BigDecimal.ZERO);
//
//            product.setIsVeg(masterProduct.getVeg() != null && masterProduct.getVeg() == 1);
//
//            product.setHasProductVariants(masterProduct.getHasOptions() != null && masterProduct.getHasOptions() == 1);
//
//            product.setImageLink(masterProduct.getPhoto());
//            product.setPhotos(masterProduct.getPhotos());
//            product.setThumbnail(masterProduct.getThumbnail());
//            product.setCreatedBy(1);
//            product.setUpdatedBy(1);
//
//            FmProduct savedProduct = productRepository.save(product);
//
//            log.info("Mapped Product. ProductId={}, Name={}", savedProduct.getProductId(), savedProduct.getProductName());
//            savedNames.add(productName);
//        }
//
//        FmMasterProductMappingResultDTO response = new FmMasterProductMappingResultDTO();
//
//        response.setOutletCategoryId(outletCategoryId);
//        response.setCategoryId(categoryId);
//        response.setCategoryName(category.getCategoryName());
//        response.setTotalMasterProducts(masterProducts.size());
//        response.setSavedCount(savedNames.size());
//        response.setSkippedCount(skippedNames.size());
//        response.setSavedProductNames(savedNames);
//        response.setSkippedProductNames(skippedNames);
//
//        log.info("Completed Master Product Mapping. Saved={}, Skipped={}", savedNames.size(), skippedNames.size());
//
//        return response;
//    }

    @Override
    @Transactional(readOnly = true)
    public FmProductUpdateResponseDto getProductById(Integer productId) {

        log.info("Fetching Product Details. ProductId={}", productId);

        FmProduct product = productRepository.findByProductIdAndIsActive(productId, "Y").orElseThrow(() -> {

            log.error("Product not found. ProductId={}", productId);

            return new ResourceNotFoundException("Product not found with id : " + productId);
        });

        FmProductUpdateResponseDto response = new FmProductUpdateResponseDto();

        response.setProductId(product.getProductId());
        response.setOutletCategoryId(product.getOutletCategoryId());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setIsVeg(product.getIsVeg());
        response.setHasProductVariants(product.getHasProductVariants());
        response.setMerchantPrice(product.getMerchantPrice());
        response.setImageLink(product.getImageLink());
        response.setPhotos(product.getPhotos());
        response.setThumbnail(product.getThumbnail());

        /*
         * Load Product Timings
         */
        List<FmProductAvailableTiming> timings = productAvailableTimingRepository.findByProductIdOrderByDayOfWeekIdAsc(productId);

        List<FmProductTimingResponseDto> timingDtos = new ArrayList<>();

        for (FmProductAvailableTiming timing : timings) {

            FmProductTimingResponseDto dto = new FmProductTimingResponseDto();

            dto.setProductAvailableTimingId(
                    timing.getProductAvailableTimingId());

            dto.setDayOfWeekId(
                    timing.getDayOfWeekId());

            String dayName = daysOfWeekRepository.findById(timing.getDayOfWeekId()).map(FmDaysOfWeek::getDayName).orElse(null);

            dto.setDayName(dayName);

            dto.setStartTime(timing.getStartTime() == null ? null : timing.getStartTime().toString());

            dto.setEndTime(timing.getEndTime() == null ? null : timing.getEndTime().toString());

            timingDtos.add(dto);
        }

        response.setTimings(timingDtos);

        log.info("Loaded {} timings for ProductId={}", timingDtos.size(), productId);

        List<FmProductVariantOption> variantOptions = productVariantOptionRepository.findByProductIdAndIsActiveTrueOrderByProductVariantOptionsIdAsc(productId);

        Map<Integer, FmProductEditVariantGroupDto> groupMap = new LinkedHashMap<>();

        for (FmProductVariantOption option : variantOptions) {

            FmProductVariantGroupValue value = productVariantGroupValueRepository.findByProductVariantGroupValuesIdAndIsActiveTrue(option.getProductVariantGroupValuesId()).orElseThrow(() -> new ResourceNotFoundException("Variant Value not found : " + option.getProductVariantGroupValuesId()));

            FmProductVariantGroup group = productVariantGroupRepository.findByProductVariantGroupsIdAndIsActiveTrue(value.getProductVariantGroupsId()).orElseThrow(() -> new ResourceNotFoundException("Variant Group not found : " + value.getProductVariantGroupsId()));

            FmProductEditVariantGroupDto groupDto = groupMap.computeIfAbsent(group.getProductVariantGroupsId(), id -> {

                FmProductEditVariantGroupDto dto = new FmProductEditVariantGroupDto();

                dto.setProductVariantGroupsId(group.getProductVariantGroupsId());
                dto.setGroupName(group.getGroupName());
                dto.setSelectionType(group.getSelectionType());
                dto.setMinSelection(group.getMinSelection());
                dto.setMaxSelection(group.getMaxSelection());
                dto.setDisplayOrder(group.getDisplayOrder());
                dto.setOptions(new ArrayList<>());

                return dto;
            });

            FmProductEditVariantOptionDto optionDto = new FmProductEditVariantOptionDto();

            optionDto.setProductVariantOptionsId(option.getProductVariantOptionsId());
            optionDto.setProductVariantGroupValuesId(option.getProductVariantGroupValuesId());
            optionDto.setVariantName(value.getVariantName());
            optionDto.setPriceType(option.getPriceType());
            optionDto.setVariantPrice(option.getVariantPrice());

            groupDto.getOptions().add(optionDto);
        }

        response.setVariantGroups(new ArrayList<>(groupMap.values()));

        log.info("Loaded {} Variant Groups for ProductId={}", groupMap.size(), productId);

        return response;


    }

    @Override
    public FmProductUpdateResponseDto updateProduct(Integer productId, FmProductUpdateRequestDto request) {

        log.info("Updating Product. ProductId={}", productId);

        validateProductUpdateRequest(request);

        FmProduct product = productRepository.findByProductIdAndIsActive(productId, "Y").orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + productId));

        /*
         * Basic Details
         */
        product.setProductName(request.getProductName().trim());
        product.setDescription(request.getDescription() == null ? "" : request.getDescription());

        product.setIsVeg(request.getIsVeg() == null ? Boolean.TRUE : request.getIsVeg());

        boolean hasVariants = Boolean.TRUE.equals(request.getHasProductVariants());

        product.setHasProductVariants(hasVariants);

        product.setImageLink(request.getImageLink());
        product.setPhotos(request.getPhotos());
        product.setThumbnail(request.getThumbnail());
        product.setUpdatedBy(1);

        if (hasVariants) {
            product.setMerchantPrice(BigDecimal.ZERO);
        } else {
            product.setMerchantPrice(request.getMerchantPrice() == null ? BigDecimal.ZERO : request.getMerchantPrice());
        }

        productRepository.save(product);

        /*
         * Update Timings
         */
        /*
         * Update Product Timings
         */
        if (request.getTimings() != null && !request.getTimings().isEmpty()) {

            updateProductTimings(productId, request.getTimings());
        }

        /*
         * Update Variant Options
         */
        if (hasVariants) {

            if (request.getVariantGroups() == null || request.getVariantGroups().isEmpty()) {

                throw new IllegalArgumentException("Variant Groups are required.");
            }

            /*
             * Validate First
             */
            for (FmProductVariantOptionGroupDto group : request.getVariantGroups()) {

                validateVariantGroup(group);

                if (group.getOptions() == null || group.getOptions().isEmpty()) {

                    throw new IllegalArgumentException("Variant Values are required.");
                }

                for (FmProductVariantOptionRequestDto option : group.getOptions()) {

                    validateVariantValue(group.getProductVariantGroupsId(), option);
                }
            }

            updateProductVariantOptions(productId, request.getVariantGroups());

        } else {

            /*
             * Remove Existing Variant Options
             */
            productVariantOptionRepository.deleteByProductId(productId);
        }

        log.info("Product updated successfully. ProductId={}", productId);

        return getProductById(productId);
    }

    /**
     * Save Product Variant Options
     */
    private void saveProductVariantOptions(Integer productId, List<FmProductVariantOptionGroupDto> variantGroups) {

        if (variantGroups == null || variantGroups.isEmpty()) {

            log.warn("No Variant Groups found for ProductId={}", productId);

            return;
        }

        log.info("Saving Variant Options. ProductId={}, Groups={}", productId, variantGroups.size());

        for (FmProductVariantOptionGroupDto group : variantGroups) {

            validateVariantGroup(group);

            if (group.getOptions() == null || group.getOptions().isEmpty()) {

                log.error("No Variant Values found for GroupId={}", group.getProductVariantGroupsId());

                throw new IllegalArgumentException("Variant Values are required.");
            }
            for (FmProductVariantOptionRequestDto option : group.getOptions()) {

                validateVariantValue(group.getProductVariantGroupsId(), option);

                if (productVariantOptionRepository.existsByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(productId, option.getProductVariantGroupValuesId())) {

                    log.warn("Variant already mapped. ProductId={}, VariantValueId={}", productId, option.getProductVariantGroupValuesId());

                    continue;
                }

                FmProductVariantOption entity =
                        FmProductVariantOptionMapper.toEntity(productId, option);

                entity.setCreatedBy(1);
                entity.setUpdatedBy(1);

                productVariantOptionRepository.save(entity);

                log.info("Variant Option saved. ProductId={}, VariantValueId={}", productId, option.getProductVariantGroupValuesId());
            }
        }

        log.info("Completed saving Variant Options. ProductId={}", productId);
    }

    private void updateProductVariantOptions(
            Integer productId,
            List<FmProductVariantOptionGroupDto> variantGroups) {

        List<FmProductVariantOption> existingOptions =
                productVariantOptionRepository
                        .findByProductIdOrderByProductVariantOptionsIdAsc(productId);

        Map<Integer, FmProductVariantOption> existingMap = new HashMap<>();

        for (FmProductVariantOption option : existingOptions) {
            existingMap.put(option.getProductVariantOptionsId(), option);
        }

        Set<Integer> processedIds = new HashSet<>();

        for (FmProductVariantOptionGroupDto group : variantGroups) {

            for (FmProductVariantOptionRequestDto requestOption : group.getOptions()) {

                /*
                 * UPDATE
                 */
                if (requestOption.getProductVariantOptionsId() != null) {

                    FmProductVariantOption entity =
                            existingMap.get(requestOption.getProductVariantOptionsId());

                    if (entity == null) {
                        throw new ResourceNotFoundException(
                                "Variant Option not found : "
                                        + requestOption.getProductVariantOptionsId());
                    }

                    // Update only changed fields

                    if (!Objects.equals(entity.getProductVariantGroupValuesId(),
                            requestOption.getProductVariantGroupValuesId())) {

                        validateVariantValue(group.getProductVariantGroupsId(), requestOption);

                        entity.setProductVariantGroupValuesId(
                                requestOption.getProductVariantGroupValuesId());
                    }

                    if (!Objects.equals(entity.getPriceType(),
                            requestOption.getPriceType())) {

                        entity.setPriceType(requestOption.getPriceType());
                    }

                    if (!Objects.equals(entity.getVariantPrice(),
                            requestOption.getVariantPrice())) {

                        entity.setVariantPrice(requestOption.getVariantPrice());
                    }

                    entity.setUpdatedBy(SYSTEM_USER);

                    productVariantOptionRepository.save(entity);

                    processedIds.add(entity.getProductVariantOptionsId());
                }

                /*
                 * INSERT
                 */
                else {

                    FmProductVariantOption entity =
                            FmProductVariantOptionMapper.toEntity(productId, requestOption);

                    entity.setCreatedBy(SYSTEM_USER);
                    entity.setUpdatedBy(SYSTEM_USER);

                    FmProductVariantOption saved =
                            productVariantOptionRepository.save(entity);

                    processedIds.add(saved.getProductVariantOptionsId());
                }
            }
        }

        /*
         * DELETE REMOVED OPTIONS
         */
        for (FmProductVariantOption existing : existingOptions) {

            if (!processedIds.contains(existing.getProductVariantOptionsId())) {

                productVariantOptionRepository.delete(existing);
            }
        }
    }
    private void updateProductTimings(
            Integer productId,
            List<FmProductTimingRequestDto> timings) {

        List<FmProductAvailableTiming> existingTimings =
                productAvailableTimingRepository
                        .findByProductIdOrderByProductAvailableTimingIdAsc(productId);

        Map<Integer, FmProductAvailableTiming> existingMap = new HashMap<>();

        for (FmProductAvailableTiming timing : existingTimings) {
            existingMap.put(timing.getProductAvailableTimingId(), timing);
        }

        Set<Integer> processedIds = new HashSet<>();

        for (FmProductTimingRequestDto requestTiming : timings) {

            daysOfWeekRepository.findById(requestTiming.getDayOfWeekId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Invalid Day Id : " + requestTiming.getDayOfWeekId()));

            LocalTime start = parseTime(requestTiming.getStartTime());

            LocalTime end = parseTime(requestTiming.getEndTime());

            if (start == null || end == null) {
                throw new IllegalArgumentException("Invalid Product Timing.");
            }

            /*
             * UPDATE
             */
            if (requestTiming.getProductAvailableTimingId() != null) {

                FmProductAvailableTiming entity =
                        existingMap.get(requestTiming.getProductAvailableTimingId());

                if (entity == null) {
                    throw new ResourceNotFoundException(
                            "Timing not found : "
                                    + requestTiming.getProductAvailableTimingId());
                }

                if (!Objects.equals(entity.getDayOfWeekId(),
                        requestTiming.getDayOfWeekId())) {

                    entity.setDayOfWeekId(requestTiming.getDayOfWeekId());
                }

                if (!Objects.equals(entity.getStartTime(), start)) {

                    entity.setStartTime(start);
                }

                if (!Objects.equals(entity.getEndTime(), end)) {

                    entity.setEndTime(end);
                }

                entity.setUpdatedBy(SYSTEM_USER);

                productAvailableTimingRepository.save(entity);

                processedIds.add(entity.getProductAvailableTimingId());
            }

            /*
             * INSERT
             */
            else {

                FmProductAvailableTiming entity =
                        new FmProductAvailableTiming();

                entity.setProductId(productId);
                entity.setDayOfWeekId(requestTiming.getDayOfWeekId());
                entity.setStartTime(start);
                entity.setEndTime(end);

                entity.setCreatedBy(SYSTEM_USER);
                entity.setUpdatedBy(SYSTEM_USER);

                FmProductAvailableTiming saved =
                        productAvailableTimingRepository.save(entity);

                processedIds.add(saved.getProductAvailableTimingId());
            }
        }

        /*
         * DELETE REMOVED TIMINGS
         */
        for (FmProductAvailableTiming timing : existingTimings) {

            if (!processedIds.contains(
                    timing.getProductAvailableTimingId())) {

                productAvailableTimingRepository.delete(timing);
            }
        }
    }

    /**
     * Validate Variant Group
     */
    private void validateVariantGroup(FmProductVariantOptionGroupDto group) {

        if (group.getProductVariantGroupsId() == null) {

            log.error("Variant Group Id is missing.");

            throw new IllegalArgumentException("Variant Group Id is required.");
        }

        productVariantGroupRepository.findByProductVariantGroupsIdAndIsActiveTrue(group.getProductVariantGroupsId()).orElseThrow(() -> {

            log.error("Variant Group not found. GroupId={}", group.getProductVariantGroupsId());

            return new IllegalArgumentException("Variant Group not found with id : " + group.getProductVariantGroupsId());
        });
    }

    /**
     * Validate Variant Value
     */
    private void validateVariantValue(Integer productVariantGroupsId, FmProductVariantOptionRequestDto option) {

        if (option.getProductVariantGroupValuesId() == null) {

            log.error("Variant Value Id is missing.");

            throw new IllegalArgumentException("Variant Value Id is required.");
        }

        productVariantGroupValueRepository.findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(option.getProductVariantGroupValuesId(), productVariantGroupsId).orElseThrow(() -> {

            log.error("Variant Value not found. GroupId={}, ValueId={}", productVariantGroupsId, option.getProductVariantGroupValuesId());

            return new IllegalArgumentException("Variant Value not found with id : " + option.getProductVariantGroupValuesId());
        });

        validatePriceType(option.getPriceType());

        if (option.getVariantPrice() == null || option.getVariantPrice().compareTo(BigDecimal.ZERO) < 0) {

            log.error("Invalid Variant Price : {}", option.getVariantPrice());

            throw new IllegalArgumentException("Variant Price should be greater than or equal to zero.");
        }
    }

    /**
     * Validate Price Type
     */
    private void validatePriceType(String priceType) {

        if (priceType == null || priceType.isBlank()) {

            log.error("Price Type is missing.");

            throw new IllegalArgumentException("Price Type is required.");
        }

        String value = priceType.trim().toUpperCase();

        if (!"MAIN".equals(value) && !"ADD".equals(value)) {

            log.error("Invalid Price Type : {}", priceType);

            throw new IllegalArgumentException("Price Type must be MAIN or ADD.");
        }
    }

    private BigDecimal resolvePrice(String productName) {

        Double price = FmProductMapper.priceMapper.get(productName);

        if (price == null || price < 0) {

            log.warn("Price not found for Product : {}", productName);

            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(price);
    }

    private void saveTimings(Integer productId, ProductEntry entry) {

        if (entry.getTimings() != null && !entry.getTimings().isEmpty()) {

            for (FmProductTimingRequestDto timing : entry.getTimings()) {

                LocalTime start = parseTime(timing.getStartTime());

                LocalTime end = parseTime(timing.getEndTime());

                if (start == null || end == null) {
                    continue;
                }

                FmProductAvailableTiming entity = new FmProductAvailableTiming();

                entity.setProductId(productId);
                daysOfWeekRepository.findById(timing.getDayOfWeekId()).orElseThrow(() -> {

                    log.error("Invalid Day Id : {}", timing.getDayOfWeekId());

                    return new IllegalArgumentException("Invalid Day Id : " + timing.getDayOfWeekId());
                });

                entity.setDayOfWeekId(timing.getDayOfWeekId());
                entity.setStartTime(start);
                entity.setEndTime(end);
                entity.setCreatedBy(1);
                entity.setUpdatedBy(1);

                productAvailableTimingRepository.save(entity);
            }

            return;
        }

        String rawTiming = entry.getCsvTiming();

        if (rawTiming == null || rawTiming.isBlank()) {

            rawTiming = FmProductMapper.timingMapper.get(entry.getProductName());
        }

        if (rawTiming == null || rawTiming.isBlank()) {
            return;
        }

        String dayName = entry.getCsvDayOfWeek();

        if (dayName == null || dayName.isBlank()) {

            dayName = FmProductMapper.dayOfWeekMapper.get(entry.getProductName());
        }
        Integer dayId = null;

        if (dayName != null && !dayName.isBlank()) {

            String finalDay = dayName.trim();

            dayId = daysOfWeekRepository.findByDayNameIgnoreCase(finalDay).map(FmDaysOfWeek::getDayId).orElse(null);
        }

        if (dayId == null) {

            log.warn("Day not found : {}", dayName);

            return;
        }


        String[] slots = rawTiming.split(",");

        for (String slot : slots) {

            String[] time = slot.trim().split("-", 2);

            if (time.length != 2) {
                continue;
            }

            LocalTime start = parseTime(time[0]);

            LocalTime end = parseTime(time[1]);

            if (start == null || end == null) {
                continue;
            }

            FmProductAvailableTiming entity = new FmProductAvailableTiming();

            entity.setProductId(productId);
            entity.setDayOfWeekId(dayId);
            entity.setStartTime(start);
            entity.setEndTime(end);
            entity.setCreatedBy(1);
            entity.setUpdatedBy(1);

            productAvailableTimingRepository.save(entity);
        }
    }

    private LocalTime parseTime(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {

            String time = value.trim();

            if (time.indexOf(':') == 1) {
                time = "0" + time;
            }

            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));

        } catch (Exception ex) {

            log.warn("Unable to parse time : {}", value);

            return null;
        }
    }

    /**
     * Validate Product Update Request
     */
    private void validateProductUpdateRequest(FmProductUpdateRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }

        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {

            throw new IllegalArgumentException("Product Name is required.");
        }

        if (Boolean.TRUE.equals(request.getHasProductVariants())) {

            if (request.getVariantGroups() == null || request.getVariantGroups().isEmpty()) {

                throw new IllegalArgumentException("Variant Groups are required.");
            }

            for (FmProductVariantOptionGroupDto group : request.getVariantGroups()) {

                validateVariantGroup(group);

                if (group.getOptions() == null || group.getOptions().isEmpty()) {

                    throw new IllegalArgumentException("Variant Values are required.");
                }

                for (FmProductVariantOptionRequestDto option : group.getOptions()) {

                    validateVariantValue(group.getProductVariantGroupsId(), option);
                }
            }

        } else {

            if (request.getMerchantPrice() == null) {

                throw new IllegalArgumentException("Merchant Price is required.");
            }

            if (request.getMerchantPrice().compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException("Merchant Price cannot be negative.");
            }
        }
    }

}
