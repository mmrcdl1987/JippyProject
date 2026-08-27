package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.enums.FmVariantBulkUploadStatus;
import com.jippy.foodandmart.exception.FileProcessingException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.mapper.FmProductVariantOptionMapper;
import com.jippy.foodandmart.projections.FmMasterProductCategoryProjection;
import com.jippy.foodandmart.projections.FmProductCategoryProjection;
import com.jippy.foodandmart.projections.FmProductPriceProjection;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.FmProductService;
import com.jippy.foodandmart.util.FmVariantExcelReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmProductServiceImpl implements FmProductService {

    private static final Integer SYSTEM_USER = 1;

    private final FmProductRepository productRepository;

    private final FmOutletCategoryRepository outletCategoryRepository;

    private final FmMasterProductRepository masterProductRepository;

    private final FmProductAvailableTimingRepository productAvailableTimingRepository;

    private final FmOutletRepository outletRepository;

    private final FmProductVariantGroupRepository variantGroupRepository;

    private final FmProductVariantGroupValueRepository variantGroupValueRepository;

    private final FmProductVariantOptionRepository variantOptionRepository;

    private final CacheInvalidateServiceImpl cacheInvalidateService;


    private final FmDaysOfWeekRepository daysOfWeekRepository;

    @Override
    @Transactional
    public FmMapToProductResult mapToProducts(FmMapToProduct request) {

        log.info("[PRODUCT-MAP] Product mapping initiated | outletId={} | categoryId={} | requestedProducts={}", request.getOutletId(), request.getCategoryId(), request.getProducts() == null ? 0 : request.getProducts().size());

        if (request.getProducts() == null || request.getProducts().isEmpty()) {

            log.warn("[PRODUCT-MAP] Product mapping failed | reason=Product list is empty");

            throw new IllegalArgumentException("Products are required.");
        }

        if (request.getOutletId() == null) {

            log.warn("[PRODUCT-MAP] Product mapping failed | reason=Outlet Id is missing");

            throw new IllegalArgumentException("Outlet Id is required.");
        }

        if (request.getCategoryId() == null) {

            log.warn("[PRODUCT-MAP] Product mapping failed | reason=Category Id is missing");

            throw new IllegalArgumentException("Category Id is required.");
        }

        FmOutletCategory outletCategory = outletCategoryRepository.findByOutletIdAndCategoryId(request.getOutletId(), request.getCategoryId()).orElseGet(() -> {

            log.info("[PRODUCT-MAP] Outlet category not found. Creating new mapping | outletId={} | categoryId={}", request.getOutletId(), request.getCategoryId());

            FmOutletCategory entity = new FmOutletCategory();

            entity.setOutletId(request.getOutletId());
            entity.setCategoryId(request.getCategoryId());
            entity.setCreatedBy(SYSTEM_USER);
            entity.setUpdatedBy(SYSTEM_USER);
            entity.setIsToggle(true);
            entity.setIsActive("Y");

            FmOutletCategory saved = outletCategoryRepository.save(entity);

            log.info("[PRODUCT-MAP] Outlet category created successfully | outletCategoryId={}", saved.getOutletCategoryId());

            return saved;
        });

        Integer outletCategoryId = outletCategory.getOutletCategoryId();

        List<String> savedNames = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();

        for (ProductEntry entry : request.getProducts()) {

            String productName = entry.getProductName() == null ? "" : entry.getProductName().trim();

            if (productName.isBlank()) {

                log.warn("[PRODUCT-MAP] Skipping product | reason=Blank product name");

                skippedNames.add("(blank)");

                continue;
            }

            if (productRepository.existsByOutletCategoryIdAndProductNameIgnoreCase(outletCategoryId, productName)) {

                log.warn("[PRODUCT-MAP] Product already mapped | outletCategoryId={} | productName={}", outletCategoryId, productName);

                skippedNames.add(productName + " (Already Exists)");

                continue;
            }

            if (entry.getMasterProductId() == null) {

                log.warn("[PRODUCT-MAP] Product mapping failed | productName={} | reason=Master Product Id missing", productName);

                throw new IllegalArgumentException("Master Product Id is required.");
            }

            FmMasterProduct masterProduct = masterProductRepository.findById(entry.getMasterProductId()).orElseThrow(() -> {

                log.warn("[PRODUCT-MAP] Master product not found | masterProductId={}", entry.getMasterProductId());

                return new ResourceNotFoundException("Master Product not found with id : " + entry.getMasterProductId());
            });

            if (!Objects.equals(masterProduct.getCategoryId(), request.getCategoryId())) {

                log.warn("[PRODUCT-MAP] Invalid master product category | masterProductId={} | expectedCategory={} | actualCategory={}", masterProduct.getMasterProductId(), request.getCategoryId(), masterProduct.getCategoryId());

                throw new IllegalArgumentException("Selected Master Product does not belong to Category Id : " + request.getCategoryId());
            }

            if (masterProduct.getPhoto() == null || masterProduct.getPhoto().isBlank()) {

                log.warn("[PRODUCT-MAP] Master product image missing | masterProductId={} | productName={}", masterProduct.getMasterProductId(), productName);

                throw new IllegalArgumentException("Master Product image missing : " + productName);
            }

            boolean hasVariants = Boolean.TRUE.equals(entry.getHasProductVariants());

            FmProduct product = new FmProduct();

            product.setOutletCategoryId(outletCategoryId);
            product.setProductName(productName);
            product.setDescription(entry.getDescription() == null ? "" : entry.getDescription());

            product.setIsVeg(entry.getIsVeg() == null ? Boolean.TRUE : entry.getIsVeg());

            product.setHasProductVariants(hasVariants);

            product.setImageLink(masterProduct.getPhoto());
//            product.setPhotos(masterProduct.getPhotos());
//            product.setThumbnail(masterProduct.getThumbnail());

            product.setCreatedBy(SYSTEM_USER);
            product.setUpdatedBy(SYSTEM_USER);

            if (hasVariants) {

                product.setMerchantPrice(BigDecimal.ZERO);

            } else {

                BigDecimal requestPrice = entry.getMerchantPrice();

                product.setMerchantPrice(requestPrice != null && requestPrice.compareTo(BigDecimal.ZERO) > 0 ? requestPrice : resolvePrice(productName));
            }

            FmProduct savedProduct = productRepository.save(product);

            if (hasVariants) {

                saveProductVariantOptions(savedProduct.getProductId(), entry.getVariantGroups());
            }

            saveTimings(savedProduct.getProductId(), entry);

            savedNames.add(productName);

            log.info("[PRODUCT-MAP] Product mapped successfully | productId={} | productName={}", savedProduct.getProductId(), savedProduct.getProductName());
        }

        //Invalidate outlet details cache
        cacheInvalidateService.invalidateCache(request.getOutletId());

        FmMapToProductResult response = new FmMapToProductResult();

        response.setSavedCount(savedNames.size());
        response.setSkippedCount(skippedNames.size());
        response.setSavedNames(savedNames);
        response.setSkippedNames(skippedNames);

        log.info("[PRODUCT-MAP] Product mapping completed | outletCategoryId={} | saved={} | skipped={}", outletCategoryId, savedNames.size(), skippedNames.size());

        return response;
    }

    @Override
    @Transactional
    public FmVariantBulkUploadResponseDto bulkUploadVariants(Integer outletId, MultipartFile file) {

        log.info("[VARIANT-BULK] START | outletId={} | file={}", outletId, file != null ? file.getOriginalFilename() : null);

        /*
         * ============================================================
         * 1. VALIDATE REQUEST
         * ============================================================
         */

        if (outletId == null || outletId <= 0) {

            log.warn("[VARIANT-BULK] INVALID_OUTLET_ID | outletId={}", outletId);

            throw new IllegalArgumentException("Valid outletId is required.");
        }

        if (file == null || file.isEmpty()) {

            log.warn("[VARIANT-BULK] EMPTY_FILE | outletId={}", outletId);

            throw new FileProcessingException("Excel file is required.");
        }

        /*
         * ============================================================
         * 2. VALIDATE OUTLET
         * ============================================================
         */

        FmOutlet outlet = outletRepository.findByOutletIdAndIsActive(outletId, "Y").orElseThrow(() -> {

            log.warn("[VARIANT-BULK] OUTLET_NOT_FOUND | outletId={}", outletId);

            return new ResourceNotFoundException("Active outlet not found with id : " + outletId);
        });

        log.info("[VARIANT-BULK] OUTLET_VALIDATED | outletId={} | outletName={}", outlet.getOutletId(), outlet.getOutletName());

        /*
         * ============================================================
         * 3. LOAD ACTIVE OUTLET CATEGORIES
         * ============================================================
         */

        List<FmOutletCategory> outletCategories = outletCategoryRepository.findByOutletIdAndIsActive(outletId, "Y");

        if (outletCategories.isEmpty()) {

            log.warn("[VARIANT-BULK] NO_ACTIVE_CATEGORIES | outletId={}", outletId);

            throw new ResourceNotFoundException("No active categories found for outlet id : " + outletId);
        }

        log.info("[VARIANT-BULK] CATEGORIES_LOADED | outletId={} | count={}", outletId, outletCategories.size());

        /*
         * ============================================================
         * 4. LOAD ACTIVE OUTLET PRODUCTS
         * ============================================================
         *
         * The repository query internally joins:
         *
         * products
         *      ↓
         * outlet_categories
         *      ↓
         * outletId
         *
         * and filters active categories/products.
         */

        List<FmProduct> outletProducts = productRepository.findActiveProductsByOutletId(outletId);

        if (outletProducts.isEmpty()) {

            log.warn("[VARIANT-BULK] NO_ACTIVE_PRODUCTS | outletId={}", outletId);

            throw new ResourceNotFoundException("No active products found for outlet id : " + outletId);
        }

        log.info("[VARIANT-BULK] PRODUCTS_LOADED | outletId={} | count={}", outletId, outletProducts.size());

        /*
         * ============================================================
         * 5. BUILD PRODUCT MAP
         * ============================================================
         *
         * Key:
         * normalized product name
         *
         * Example:
         *
         * "Chicken Biryani"
         *       ↓
         * "chicken biryani"
         */

        Map<String, FmProduct> productMap = outletProducts.stream().filter(product -> product.getProductName() != null && !product.getProductName().isBlank()).collect(Collectors.toMap(product -> normalizeName(product.getProductName()), Function.identity(), (existing, duplicate) -> existing));

        log.info("[VARIANT-BULK] PRODUCT_MAP_CREATED | outletId={} | count={}", outletId, productMap.size());

        /*
         * ============================================================
         * 6. LOAD ACTIVE MASTER VARIANT GROUPS
         * ============================================================
         */

        List<FmProductVariantGroup> variantGroups = variantGroupRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        if (variantGroups.isEmpty()) {

            log.warn("[VARIANT-BULK] NO_ACTIVE_VARIANT_GROUPS | outletId={}", outletId);

            throw new ResourceNotFoundException("No active variant groups found.");
        }

        log.info("[VARIANT-BULK] VARIANT_GROUPS_LOADED | count={}", variantGroups.size());

        /*
         * ============================================================
         * 7. BUILD VARIANT GROUP MAP
         * ============================================================
         *
         * Example:
         *
         * "Add-ons" → group entity
         * "Size"    → group entity
         */

        Map<String, FmProductVariantGroup> variantGroupMap = variantGroups.stream().filter(group -> group.getGroupName() != null && !group.getGroupName().isBlank()).collect(Collectors.toMap(group -> normalizeName(group.getGroupName()), Function.identity(), (existing, duplicate) -> existing));

        log.info("[VARIANT-BULK] VARIANT_GROUP_MAP_CREATED | count={}", variantGroupMap.size());

        /*
         * ============================================================
         * 8. LOAD ACTIVE VARIANT GROUP VALUES
         * ============================================================
         */

        List<FmProductVariantGroupValue> variantGroupValues = variantGroupValueRepository.findAllActiveValues();

        if (variantGroupValues.isEmpty()) {

            log.warn("[VARIANT-BULK] NO_ACTIVE_VARIANT_VALUES | outletId={}", outletId);

            throw new ResourceNotFoundException("No active variant group values found.");
        }

        log.info("[VARIANT-BULK] VARIANT_VALUES_LOADED | count={}", variantGroupValues.size());

        /*
         * ============================================================
         * 9. BUILD VARIANT GROUP ID MAP
         * ============================================================
         *
         * Key:
         * productVariantGroupsId
         *
         * This avoids searching variantGroups repeatedly
         * for every variant value.
         */
        Map<Integer, FmProductVariantGroup> variantGroupById = variantGroups.stream().collect(Collectors.toMap(FmProductVariantGroup::getProductVariantGroupsId, Function.identity(), (existing, duplicate) -> existing));

        log.info("[VARIANT-BULK] VARIANT_GROUP_ID_MAP_CREATED | count={}", variantGroupById.size());

        /*
         * ============================================================
         * 10. BUILD GROUP + VALUE MAP
         * ============================================================
         *
         * Key:
         *
         * normalizedGroupName + "|" + normalizedVariantName
         *
         * Examples:
         *
         * "add-ons|extra cheese"
         * "size|medium"
         */
        Map<String, FmProductVariantGroupValue> variantValueMap = new HashMap<>();

        for (FmProductVariantGroupValue value : variantGroupValues) {

            if (value.getVariantName() == null || value.getVariantName().isBlank()) {
                continue;
            }

            FmProductVariantGroup group = variantGroupById.get(value.getProductVariantGroupsId());

            if (group == null || group.getGroupName() == null || group.getGroupName().isBlank()) {
                continue;
            }

            String mapKey = normalizeName(group.getGroupName()) + "|" + normalizeName(value.getVariantName());

            variantValueMap.putIfAbsent(mapKey, value);
        }

        log.info("[VARIANT-BULK] VARIANT_VALUE_MAP_CREATED | count={}", variantValueMap.size());

        /*
         * ============================================================
         * 10. READ EXCEL
         * ============================================================
         */

        List<FmVariantBulkUploadRowDto> rows = FmVariantExcelReader.read(file);

        log.info("[VARIANT-BULK] EXCEL_READ | outletId={} | rows={}", outletId, rows.size());

        /*
         * ============================================================
         * 11. PROCESS EACH ROW
         * ============================================================
         */

        List<FmVariantBulkUploadResultDto> results = new ArrayList<>();

        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        for (FmVariantBulkUploadRowDto row : rows) {

            FmVariantBulkUploadResultDto result = new FmVariantBulkUploadResultDto();

            result.setRowNumber(row.getRowNumber());
            result.setProductName(row.getProductName());
            result.setVariantGroupName(row.getVariantGroupName());
            result.setVariantGroupValue(row.getVariantGroupValue());
            result.setPriceType(row.getPriceType());
            result.setVariantPrice(row.getVariantPrice());

            /*
             * --------------------------------------------------------
             * 11.1 VALIDATE PRODUCT NAME
             * --------------------------------------------------------
             */

            if (row.getProductName() == null || row.getProductName().isBlank()) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Product name is required.");

                results.add(result);
                skippedCount++;
                continue;
            }

            FmProduct product = productMap.get(normalizeName(row.getProductName()));

            if (product == null) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Product name not found for outlet.");

                results.add(result);
                skippedCount++;

                log.warn("[VARIANT-BULK] PRODUCT_NOT_FOUND | outletId={} | row={} | productName={}", outletId, row.getRowNumber(), row.getProductName());

                continue;
            }

            result.setProductId(product.getProductId());

            /*
             * --------------------------------------------------------
             * 11.2 VALIDATE VARIANT GROUP
             * --------------------------------------------------------
             */

            if (row.getVariantGroupName() == null || row.getVariantGroupName().isBlank()) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant group name is required.");

                results.add(result);
                skippedCount++;
                continue;
            }

            FmProductVariantGroup variantGroup = variantGroupMap.get(normalizeName(row.getVariantGroupName()));

            if (variantGroup == null) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant group name not found.");

                results.add(result);
                skippedCount++;

                log.warn("[VARIANT-BULK] VARIANT_GROUP_NOT_FOUND | row={} | groupName={}", row.getRowNumber(), row.getVariantGroupName());

                continue;
            }

            result.setVariantGroupId(variantGroup.getProductVariantGroupsId());

            /*
             * --------------------------------------------------------
             * 11.3 VALIDATE VARIANT GROUP VALUE
             * --------------------------------------------------------
             */

            if (row.getVariantGroupValue() == null || row.getVariantGroupValue().isBlank()) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant group value is required.");

                results.add(result);
                skippedCount++;
                continue;
            }

            String valueMapKey = normalizeName(variantGroup.getGroupName()) + "|" + normalizeName(row.getVariantGroupValue());

            FmProductVariantGroupValue variantValue = variantValueMap.get(valueMapKey);

            if (variantValue == null) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant group value not found for the specified variant group.");

                results.add(result);
                skippedCount++;

                log.warn("[VARIANT-BULK] VARIANT_VALUE_NOT_FOUND | row={} | group={} | value={}", row.getRowNumber(), row.getVariantGroupName(), row.getVariantGroupValue());

                continue;
            }

            result.setVariantGroupValueId(variantValue.getProductVariantGroupValuesId());

            /*
             * --------------------------------------------------------
             * 11.4 VALIDATE PRICE TYPE
             * --------------------------------------------------------
             */

            String priceType = row.getPriceType() == null ? null : row.getPriceType().trim().toUpperCase(Locale.ROOT);

            if (!"MAIN".equals(priceType) && !"ADD".equals(priceType)) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Price type must be MAIN or ADD.");

                results.add(result);
                skippedCount++;
                continue;
            }

            /*
             * --------------------------------------------------------
             * 11.5 VALIDATE VARIANT PRICE
             * --------------------------------------------------------
             */

            if (row.getVariantPrice() == null) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant price is required.");

                results.add(result);
                skippedCount++;
                continue;
            }

            if (row.getVariantPrice().compareTo(BigDecimal.ZERO) < 0) {

                result.setStatus(FmVariantBulkUploadStatus.SKIPPED);

                result.setMessage("Variant price cannot be negative.");

                results.add(result);
                skippedCount++;
                continue;
            }

            /*
             * --------------------------------------------------------
             * 11.6 CHECK EXISTING VARIANT OPTION
             * --------------------------------------------------------
             */

            Optional<FmProductVariantOption> existingOption = variantOptionRepository.findByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(product.getProductId(), variantValue.getProductVariantGroupValuesId());

            /*
             * --------------------------------------------------------
             * 11.7 UPDATE EXISTING OPTION
             * --------------------------------------------------------
             */

            if (existingOption.isPresent()) {

                FmProductVariantOption option = existingOption.get();

                option.setPriceType(priceType);
                option.setVariantPrice(row.getVariantPrice());

                /*
                 * @PreUpdate already updates updatedAt.
                 * No need to set updatedAt manually.
                 */
                variantOptionRepository.save(option);

                result.setStatus(FmVariantBulkUploadStatus.UPDATED);

                result.setMessage("Variant updated successfully.");

                results.add(result);
                updatedCount++;

                log.info("[VARIANT-BULK] UPDATED | row={} | productId={} | variantOptionId={}", row.getRowNumber(), product.getProductId(), option.getProductVariantOptionsId());

                continue;
            }

            /*
             * --------------------------------------------------------
             * 11.8 CREATE NEW OPTION
             * --------------------------------------------------------
             */

            FmProductVariantOptionRequestDto optionRequest = new FmProductVariantOptionRequestDto();

            optionRequest.setProductVariantGroupValuesId(variantValue.getProductVariantGroupValuesId());

            optionRequest.setPriceType(priceType);

            optionRequest.setVariantPrice(row.getVariantPrice());

            FmProductVariantOption option = FmProductVariantOptionMapper.toEntity(product.getProductId(), optionRequest);

            variantOptionRepository.save(option);

            /*
             * --------------------------------------------------------
             * 11.9 ENABLE PRODUCT VARIANTS
             * --------------------------------------------------------
             *
             * If this is the first variant being added to the product,
             * make sure products.has_product_variants = true.
             */

            if (!Boolean.TRUE.equals(product.getHasProductVariants())) {

                product.setHasProductVariants(true);

                productRepository.save(product);

                log.info("[VARIANT-BULK] PRODUCT_VARIANTS_ENABLED | outletId={} | productId={}", outletId, product.getProductId());
            }

            result.setStatus(FmVariantBulkUploadStatus.CREATED);

            result.setMessage("Variant added successfully.");

            results.add(result);

            createdCount++;

            log.info("[VARIANT-BULK] CREATED | row={} | productId={} | variantOptionId={} | groupValueId={}", row.getRowNumber(), product.getProductId(), option.getProductVariantOptionsId(), variantValue.getProductVariantGroupValuesId());
        }
        /*
         * ============================================================
         * 12. FINAL RESPONSE
         * ============================================================
         */

        FmVariantBulkUploadResponseDto response = new FmVariantBulkUploadResponseDto();

        response.setSuccess(true);

        response.setMessage("Variant bulk upload completed successfully.");

        response.setOutletId(outletId);

        response.setTotalRows(rows.size());

        response.setCreatedCount(createdCount);

        response.setUpdatedCount(updatedCount);

        response.setSkippedCount(skippedCount);

        response.setResults(results);

        log.info("[VARIANT-BULK] COMPLETED | outletId={} | total={} | created={} | updated={} | skipped={}", outletId, rows.size(), createdCount, updatedCount, skippedCount);

        return response;
    }

    private String normalizeName(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }


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

        log.info("[PRODUCT] Fetch product details initiated | productId={}", productId);

        FmProduct product = productRepository.findByProductIdAndIsActive(productId, "Y").orElseThrow(() -> {

            log.warn("[PRODUCT] Product not found | productId={}", productId);

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
//        response.setPhotos(product.getPhotos());
//        response.setThumbnail(product.getThumbnail());

        /*
         * Product Timings
         */
        List<FmProductAvailableTiming> timings = productAvailableTimingRepository.findByProductIdOrderByDayOfWeekIdAsc(productId);

        List<FmProductTimingResponseDto> timingDtos = new ArrayList<>();

        for (FmProductAvailableTiming timing : timings) {

            FmProductTimingResponseDto dto = new FmProductTimingResponseDto();

            dto.setProductAvailableTimingId(timing.getProductAvailableTimingId());

            dto.setDayOfWeekId(timing.getDayOfWeekId());

            String dayName = daysOfWeekRepository.findById(timing.getDayOfWeekId()).map(FmDaysOfWeek::getDayName).orElse(null);

            dto.setDayName(dayName);

            dto.setStartTime(timing.getStartTime() == null ? null : timing.getStartTime().toString());

            dto.setEndTime(timing.getEndTime() == null ? null : timing.getEndTime().toString());

            timingDtos.add(dto);
        }

        response.setTimings(timingDtos);

        log.info("[PRODUCT] Product timings loaded | productId={} | timingCount={}", productId, timingDtos.size());

        /*
         * Variant Groups
         */
        List<FmProductVariantOption> variantOptions = variantOptionRepository.findByProductIdAndIsActiveTrueOrderByProductVariantOptionsIdAsc(productId);

        Map<Integer, FmProductEditVariantGroupDto> groupMap = new LinkedHashMap<>();

        for (FmProductVariantOption option : variantOptions) {

            FmProductVariantGroupValue value = variantGroupValueRepository.findByProductVariantGroupValuesIdAndIsActiveTrue(option.getProductVariantGroupValuesId()).orElseThrow(() -> {

                log.warn("[PRODUCT] Variant value not found | variantValueId={}", option.getProductVariantGroupValuesId());

                return new ResourceNotFoundException("Variant Value not found : " + option.getProductVariantGroupValuesId());
            });

            FmProductVariantGroup group = variantGroupRepository.findByProductVariantGroupsIdAndIsActiveTrue(value.getProductVariantGroupsId()).orElseThrow(() -> {

                log.warn("[PRODUCT] Variant group not found | variantGroupId={}", value.getProductVariantGroupsId());

                return new ResourceNotFoundException("Variant Group not found : " + value.getProductVariantGroupsId());
            });

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

        log.info("[PRODUCT] Product variant groups loaded | productId={} | groupCount={}", productId, groupMap.size());

        log.info("[PRODUCT] Product details fetched successfully | productId={}", productId);

        return response;
    }

    @Override
    public FmProductUpdateResponseDto updateProduct(Integer productId, FmProductUpdateRequestDto request) {

        log.info("Updating Product. ProductId={}", productId);

        validateProductUpdateRequest(request);

        FmProduct product = productRepository.findByProductIdAndIsActive(productId, "Y")
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + productId));

        /*
         * ============================================================
         * Basic Product Details
         * ============================================================
         */
        product.setProductName(request.getProductName().trim());
        product.setDescription(request.getDescription() == null ? "" : request.getDescription());

        product.setIsVeg(request.getIsVeg() == null ? Boolean.TRUE : request.getIsVeg());

        boolean hasVariants = Boolean.TRUE.equals(request.getHasProductVariants());

        product.setHasProductVariants(hasVariants);

        product.setImageLink(request.getImageLink());
//        product.setPhotos(request.getPhotos());
//        product.setThumbnail(request.getThumbnail());
        /*
         * ============================================================
         * Update Outlet Category
         *
         * The outlet_category_id comes from the request JSON.
         *
         * Example:
         * "outletCategoryId": 79
         *
         * This value will be updated in:
         *
         * jippy_fm.products.outlet_category_id
         *
         * for the product identified by the productId
         * from the URL.
         * ============================================================
         */
        if (request.getOutletCategoryId() != null) {
            product.setOutletCategoryId(request.getOutletCategoryId());
        }

        product.setUpdatedBy(1);

        /*
         * ============================================================
         * Merchant Price
         * ============================================================
         */
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
         * ============================================================
         * Update Product Timings
         * ============================================================
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
            variantOptionRepository.deleteByProductId(productId);
        }

        log.info("Product updated successfully. ProductId={}", productId);

        //Invalidate outlet details cache
        Optional<FmOutletCategory> outletCategory = outletCategoryRepository.findByOutletCategoryId(product.getOutletCategoryId());

        outletCategory.ifPresent(fmOutletCategory -> cacheInvalidateService.invalidateCache(fmOutletCategory.getOutletId()));

        return getProductById(productId);
    }

    @Override
    public boolean existsProductInOutlet(Integer outletId, Integer productId) {

        log.info("Validating product belongs to outlet. outletId={}, productId={}", outletId, productId);

        boolean exists = productRepository.existsProductInOutlet(outletId, productId);

        log.info("Product validation completed. outletId={}, productId={}, exists={}", outletId, productId, exists);

        return exists;
    }

    @Override
    public List<Integer> getActiveProductIdsByOutlet(Integer outletId) {

        log.info("Fetching active product ids. outletId={}", outletId);

        List<Integer> productIds = productRepository.findActiveProductIdsByOutlet(outletId);

        log.info("Fetched {} active products. outletId={}", productIds.size(), outletId);

        return productIds;
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

                if (variantOptionRepository.existsByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(productId, option.getProductVariantGroupValuesId())) {

                    log.warn("Variant already mapped. ProductId={}, VariantValueId={}", productId, option.getProductVariantGroupValuesId());

                    continue;
                }

                FmProductVariantOption entity = FmProductVariantOptionMapper.toEntity(productId, option);

                entity.setCreatedBy(1);
                entity.setUpdatedBy(1);

                variantOptionRepository.save(entity);

                log.info("Variant Option saved. ProductId={}, VariantValueId={}", productId, option.getProductVariantGroupValuesId());
            }
        }

        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);


        log.info("Completed saving Variant Options. ProductId={}", productId);
    }

    private void updateProductVariantOptions(Integer productId, List<FmProductVariantOptionGroupDto> variantGroups) {

        List<FmProductVariantOption> existingOptions = variantOptionRepository.findByProductIdOrderByProductVariantOptionsIdAsc(productId);

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

                    FmProductVariantOption entity = existingMap.get(requestOption.getProductVariantOptionsId());

                    if (entity == null) {
                        throw new ResourceNotFoundException("Variant Option not found : " + requestOption.getProductVariantOptionsId());
                    }

                    // Update only changed fields

                    if (!Objects.equals(entity.getProductVariantGroupValuesId(), requestOption.getProductVariantGroupValuesId())) {

                        validateVariantValue(group.getProductVariantGroupsId(), requestOption);

                        entity.setProductVariantGroupValuesId(requestOption.getProductVariantGroupValuesId());
                    }

                    if (!Objects.equals(entity.getPriceType(), requestOption.getPriceType())) {

                        entity.setPriceType(requestOption.getPriceType());
                    }

                    if (!Objects.equals(entity.getVariantPrice(), requestOption.getVariantPrice())) {

                        entity.setVariantPrice(requestOption.getVariantPrice());
                    }

                    entity.setUpdatedBy(SYSTEM_USER);

                    variantOptionRepository.save(entity);

                    processedIds.add(entity.getProductVariantOptionsId());
                }

                /*
                 * INSERT
                 */
                else {

                    FmProductVariantOption entity = FmProductVariantOptionMapper.toEntity(productId, requestOption);

                    entity.setCreatedBy(SYSTEM_USER);
                    entity.setUpdatedBy(SYSTEM_USER);

                    FmProductVariantOption saved = variantOptionRepository.save(entity);

                    processedIds.add(saved.getProductVariantOptionsId());
                }
            }
        }

        /*
         * DELETE REMOVED OPTIONS
         */
        for (FmProductVariantOption existing : existingOptions) {

            if (!processedIds.contains(existing.getProductVariantOptionsId())) {

                variantOptionRepository.delete(existing);
            }
        }

        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);
    }

    private void updateProductTimings(Integer productId, List<FmProductTimingRequestDto> timings) {

        List<FmProductAvailableTiming> existingTimings = productAvailableTimingRepository.findByProductIdOrderByProductAvailableTimingIdAsc(productId);

        Map<Integer, FmProductAvailableTiming> existingMap = new HashMap<>();

        for (FmProductAvailableTiming timing : existingTimings) {
            existingMap.put(timing.getProductAvailableTimingId(), timing);
        }

        Set<Integer> processedIds = new HashSet<>();

        for (FmProductTimingRequestDto requestTiming : timings) {

            daysOfWeekRepository.findById(requestTiming.getDayOfWeekId()).orElseThrow(() -> new IllegalArgumentException("Invalid Day Id : " + requestTiming.getDayOfWeekId()));

            LocalTime start = parseTime(requestTiming.getStartTime());

            LocalTime end = parseTime(requestTiming.getEndTime());

            if (start == null || end == null) {
                throw new IllegalArgumentException("Invalid Product Timing.");
            }

            /*
             * UPDATE
             */
            if (requestTiming.getProductAvailableTimingId() != null) {

                FmProductAvailableTiming entity = existingMap.get(requestTiming.getProductAvailableTimingId());

                if (entity == null) {
                    throw new ResourceNotFoundException("Timing not found : " + requestTiming.getProductAvailableTimingId());
                }

                if (!Objects.equals(entity.getDayOfWeekId(), requestTiming.getDayOfWeekId())) {

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

                FmProductAvailableTiming entity = new FmProductAvailableTiming();

                entity.setProductId(productId);
                entity.setDayOfWeekId(requestTiming.getDayOfWeekId());
                entity.setStartTime(start);
                entity.setEndTime(end);

                entity.setCreatedBy(SYSTEM_USER);
                entity.setUpdatedBy(SYSTEM_USER);

                FmProductAvailableTiming saved = productAvailableTimingRepository.save(entity);

                processedIds.add(saved.getProductAvailableTimingId());
            }
        }

        /*
         * DELETE REMOVED TIMINGS
         */
        for (FmProductAvailableTiming timing : existingTimings) {

            if (!processedIds.contains(timing.getProductAvailableTimingId())) {

                productAvailableTimingRepository.delete(timing);
            }
        }
        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);
    }

    /**
     * Validate Variant Group
     */
    private void validateVariantGroup(FmProductVariantOptionGroupDto group) {

        if (group.getProductVariantGroupsId() == null) {

            log.error("Variant Group Id is missing.");

            throw new IllegalArgumentException("Variant Group Id is required.");
        }

        variantGroupRepository.findByProductVariantGroupsIdAndIsActiveTrue(group.getProductVariantGroupsId()).orElseThrow(() -> {

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

        variantGroupValueRepository.findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(option.getProductVariantGroupValuesId(), productVariantGroupsId).orElseThrow(() -> {

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

        // Invalidate outlet details cache
        Integer outletId = cacheInvalidateService.getOutletIdForProduct(productId);
        cacheInvalidateService.invalidateCache(outletId);
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

    @Transactional(readOnly = true)
    @Override
    public List<FmProductPriceResponse> getProductsByOutlet(Integer outletId) {

        log.info("Fetching products for outletId : {}", outletId);

        List<FmProductPriceProjection> projections = productRepository.findProductsByOutletId(outletId);

        if (projections.isEmpty()) {
            log.warn("No products found for outletId : {}", outletId);
            throw new ResourceNotFoundException("No products found for outletId : " + outletId);
        }

        List<FmProductPriceResponse> response = projections.stream().map(product -> FmProductPriceResponse.builder().productId(product.getProductId()).productName(product.getProductName()).variantId(product.getVariantId()).variantName(product.getVariantName()).merchantPrice(product.getMerchantPrice()).onlinePrice(product.getOnlinePrice()).build()).toList();

        log.info("Successfully fetched {} products for outletId {}", response.size(), outletId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FmProductDetailResponse getProductDetailById(Integer productId) {
        {

            log.info("[PRODUCT-DETAIL] SERVICE_START | productId={}", productId);

            /*
             * ============================================================
             * 1. VALIDATE PRODUCT ID
             * ============================================================
             */
            if (productId == null || productId <= 0) {

                log.error("[PRODUCT-DETAIL] INVALID_PRODUCT_ID | productId={}", productId);

                throw new InvalidRequestException("Product id must be greater than zero.");
            }

            /*
             * ============================================================
             * 2. FETCH PRODUCT
             * ============================================================
             */
            FmProduct product = productRepository.findById(productId).orElseThrow(() -> {

                log.error("[PRODUCT-DETAIL] PRODUCT_NOT_FOUND | productId={}", productId);

                return new ResourceNotFoundException("Product not found for id: " + productId);
            });

            /*
             * ============================================================
             * 3. BUILD BASIC PRODUCT RESPONSE
             * ============================================================
             */
            FmProductDetailResponse response = new FmProductDetailResponse();

            response.setProductId(product.getProductId());

            response.setProductName(product.getProductName());

            response.setMerchantPrice(product.getMerchantPrice());

            response.setImageLink(
                    product.getImageLink()
            );


            response.setHasProductVariants(Boolean.TRUE.equals(product.getHasProductVariants()));

            /*
             * ============================================================
             * 4. PRODUCT WITHOUT VARIANTS
             * ============================================================
             */
            if (!Boolean.TRUE.equals(product.getHasProductVariants())) {

                response.setVariantGroups(new ArrayList<>());

                log.info("[PRODUCT-DETAIL] NO_VARIANTS | productId={}", productId);

                return response;
            }

            /*
             * ============================================================
             * 5. FETCH ACTIVE VARIANT OPTIONS
             * ============================================================
             */
            List<FmProductVariantOption> options = variantOptionRepository.findByProductIdAndIsActiveTrueOrderByProductVariantOptionsIdAsc(productId);

            /*
             * ============================================================
             * 6. BUILD GROUP RESPONSE
             * ============================================================
             */
            Map<Integer, FmProductVariantGroupDetailResponse> groupMap = new LinkedHashMap<>();

            for (FmProductVariantOption option : options) {

                /*
                 * --------------------------------------------------------
                 * 6.1 GROUP VALUE
                 * --------------------------------------------------------
                 */
                FmProductVariantGroupValue groupValue = option.getProductVariantGroupValue();

                if (groupValue == null) {

                    log.warn("[PRODUCT-DETAIL] GROUP_VALUE_NOT_FOUND | productId={} | optionId={}", productId, option.getProductVariantOptionsId());

                    continue;
                }

                /*
                 * --------------------------------------------------------
                 * 6.2 GROUP
                 * --------------------------------------------------------
                 */
                FmProductVariantGroup group = groupValue.getProductVariantGroup();

                if (group == null) {

                    log.warn("[PRODUCT-DETAIL] GROUP_NOT_FOUND | productId={} | optionId={}", productId, option.getProductVariantOptionsId());

                    continue;
                }

                /*
                 * --------------------------------------------------------
                 * 6.3 IGNORE INACTIVE GROUP
                 * --------------------------------------------------------
                 */
                if (!Boolean.TRUE.equals(group.getIsActive())) {

                    continue;
                }

                Integer groupId = group.getProductVariantGroupsId();

                /*
                 * --------------------------------------------------------
                 * 6.4 CREATE GROUP IF NOT PRESENT
                 * --------------------------------------------------------
                 */
                FmProductVariantGroupDetailResponse groupDto = groupMap.computeIfAbsent(groupId, key -> {

                    FmProductVariantGroupDetailResponse dto = new FmProductVariantGroupDetailResponse();

                    dto.setProductVariantGroupsId(group.getProductVariantGroupsId());

                    dto.setGroupName(group.getGroupName());

                    dto.setOptions(new ArrayList<>());

                    return dto;
                });

                /*
                 * --------------------------------------------------------
                 * 6.5 BUILD OPTION RESPONSE
                 * --------------------------------------------------------
                 */
                FmProductVariantOptionDetailResponse optionDto = new FmProductVariantOptionDetailResponse();

                optionDto.setProductVariantOptionsId(option.getProductVariantOptionsId());

                optionDto.setProductVariantGroupValuesId(option.getProductVariantGroupValuesId());

                optionDto.setVariantName(groupValue.getVariantName());

                optionDto.setPriceType(option.getPriceType());

                optionDto.setVariantPrice(option.getVariantPrice());

                /*
                 * --------------------------------------------------------
                 * 6.6 ADD OPTION TO GROUP
                 * --------------------------------------------------------
                 */
                groupDto.getOptions().add(optionDto);
            }

            /*
             * ============================================================
             * 7. SET GROUPS INTO RESPONSE
             * ============================================================
             */
            response.setVariantGroups(new ArrayList<>(groupMap.values()));

            log.info("[PRODUCT-DETAIL] SERVICE_SUCCESS | productId={} | groups={} | options={}", productId, response.getVariantGroups().size(), options.size());

            return response;
        }
    }
    //    =================================================================================================
    @Override
    @Transactional(readOnly = true)
    public Object getCategoryForProductByProductType(String productName, String productType) {

        log.info(
                "[GET_CATEGORY_FOR_PRODUCT] START | productName={} | productType={}",
                productName,
                productType
        );

        /*
         * ============================================================
         * Validate Product Name
         * ============================================================
         */
        if (productName == null || productName.trim().isEmpty()) {

            log.error(
                    "[GET_CATEGORY_FOR_PRODUCT] Product name is empty"
            );

            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        /*
         * ============================================================
         * Validate Product Type
         * ============================================================
         */
        if (productType == null || productType.trim().isEmpty()) {

            log.error(
                    "[GET_CATEGORY_FOR_PRODUCT] Product type is empty"
            );

            throw new IllegalArgumentException(
                    "Product type is required."
            );
        }

        String normalizedProductName = productName.trim();

        String normalizedProductType = productType.trim().toUpperCase();


        /*
         * ============================================================
         * PRODUCT
         * ============================================================
         */
        if (FmAppConstants.PRODUCT_TYPE_PRODUCT.equals(normalizedProductType)) {

            log.info(
                    "[GET_CATEGORY_FOR_PRODUCT] Fetching PRODUCT | productName={}",
                    normalizedProductName
            );

            List<FmProductCategoryProjection> projections =
                    productRepository.findProductCategoryDetails(
                            normalizedProductName
                    );

            if (projections == null || projections.isEmpty()) {

                log.error(
                        "[GET_CATEGORY_FOR_PRODUCT] Product not found | productName={}",
                        normalizedProductName
                );

                throw new ResourceNotFoundException(
                        "Product not found with name : "
                                + normalizedProductName
                );
            }

            /*
             * ========================================================
             * Projection -> DTO
             * ========================================================
             */
            List<FmProductCategoryResponseDto> response = new ArrayList<>();

            for (FmProductCategoryProjection projection : projections) {

                FmProductCategoryResponseDto dto =
                        FmProductMapper.mapProductCategoryProjectionToDto(
                                projection
                        );

                response.add(dto);
            }

            log.info(
                    "[GET_CATEGORY_FOR_PRODUCT] PRODUCT SUCCESS | count={}",
                    response.size()
            );

            return response;
        }


        /*
         * ============================================================
         * MASTER PRODUCT
         * ============================================================
         */
        if (FmAppConstants.PRODUCT_TYPE_MASTER_PRODUCT.equals(normalizedProductType)) {

            log.info(
                    "[GET_CATEGORY_FOR_PRODUCT] Fetching MASTERPRODUCT | productName={}",
                    normalizedProductName
            );

            List<FmMasterProductCategoryProjection> projections =
                    productRepository.findMasterProductCategoryDetails(
                            normalizedProductName
                    );

            if (projections == null || projections.isEmpty()) {

                log.error(
                        "[GET_CATEGORY_FOR_PRODUCT] Master Product not found | productName={}",
                        normalizedProductName
                );

                throw new ResourceNotFoundException(
                        "Master Product not found with name : "
                                + normalizedProductName
                );
            }

            /*
             * ========================================================
             * Projection -> DTO
             * ========================================================
             */
            List<FmMasterProductCategoryResponseDto> response =
                    new ArrayList<>();

            for (FmMasterProductCategoryProjection projection : projections) {

                FmMasterProductCategoryResponseDto dto =
                        FmProductMapper.mapMasterProductCategoryProjectionToDto(
                                projection
                        );

                response.add(dto);
            }

            log.info(
                    "[GET_CATEGORY_FOR_PRODUCT] MASTERPRODUCT SUCCESS | count={}",
                    response.size()
            );

            return response;
        }


        /*
         * ============================================================
         * INVALID PRODUCT TYPE
         * ============================================================
         */
        log.error(
                "[GET_CATEGORY_FOR_PRODUCT] Invalid productType={}",
                productType
        );

        throw new IllegalArgumentException(
                "Invalid product type. Allowed values are PRODUCT or MASTERPRODUCT."
        );
    }
    @Override
    @Transactional
    public FmProductCategoryUpdateResponseDto updateCategoryForProductByProductType(
            FmProductCategoryUpdateRequestDto request) {

        log.info(
                "[UPDATE_CATEGORY_FOR_PRODUCT] START | productName={} | productType={} | updatedCategoryId={}",
                request.getProductName(),
                request.getProductType(),
                request.getUpdatedCategoryId()
        );

        /*
         * ============================================================
         * Normalize input
         *
         * trim() removes spaces from beginning and end.
         *
         * toUpperCase() makes PRODUCT/product/Product equivalent.
         * ============================================================
         */
        String productName = request.getProductName().trim();

        String productType = request.getProductType().trim().toUpperCase();

        Integer updatedCategoryId = request.getUpdatedCategoryId();

        /*
         * ============================================================
         * Validate Category
         *
         * Make sure the new category actually exists.
         * or
         * "Does this category ID exist in the categories table?"
         * ============================================================
         */
        long categoryCount = productRepository.countCategoryById(
                updatedCategoryId
        );

        if (categoryCount == 0) {

            log.error(
                    "[UPDATE_CATEGORY_FOR_PRODUCT] Category not found | categoryId={}",
                    updatedCategoryId
            );

            throw new ResourceNotFoundException("Category not found with id : "
                    + updatedCategoryId
            );
        }


        /*
         * ============================================================
         * PRODUCT
         *
         * Example:
         *
         * productName = Lemon Soda
         *
         * products:
         * product_id = 160
         * outlet_category_id = 80
         *
         * Then:
         *
         * outlet_categories:
         * outlet_category_id = 80
         *
         * UPDATE category_id
         * ============================================================
         */
        if (FmAppConstants.PRODUCT_TYPE_PRODUCT.equals(productType)) {

            log.info(
                    "[UPDATE_CATEGORY_FOR_PRODUCT] Updating PRODUCT | productName={}",
                    productName
            );

            List<Integer> outletCategoryIds =
                    productRepository.findOutletCategoryIdsByProductName(
                            productName
                    );

            if (outletCategoryIds == null || outletCategoryIds.isEmpty()) {

                log.error(
                        "[UPDATE_CATEGORY_FOR_PRODUCT] Product not found | productName={}",
                        productName
                );

                throw new ResourceNotFoundException(
                        "Product not found with name : "
                                + productName
                );
            }

            int totalUpdatedRecords = 0;

            /*
             * ========================================================
             * Update every outlet_category_id associated
             * with the matching product name.
             * ========================================================
             */
            for (Integer outletCategoryId : outletCategoryIds) {

                if (outletCategoryId == null) {

                    log.warn(
                            "[UPDATE_CATEGORY_FOR_PRODUCT] Outlet Category ID is null | productName={}",
                            productName
                    );

                    continue;
                }

                int updatedRecords =
                        productRepository.updateOutletCategoryId(
                                outletCategoryId,
                                updatedCategoryId
                        );

                totalUpdatedRecords = totalUpdatedRecords + updatedRecords;
            }

            if (totalUpdatedRecords == 0) {

                log.error(
                        "[UPDATE_CATEGORY_FOR_PRODUCT] Outlet category update failed | productName={}",
                        productName
                );

                throw new ResourceNotFoundException(
                        "Outlet Category not found for product : "
                                + productName
                );
            }

            log.info(
                    "[UPDATE_CATEGORY_FOR_PRODUCT] PRODUCT category updated successfully | productName={} | updatedCategoryId={} | records={}",
                    productName,
                    updatedCategoryId,
                    totalUpdatedRecords
            );

            return FmProductMapper.mapCategoryUpdateResponse(
                    productType,
                    productName,
                    updatedCategoryId,
                    totalUpdatedRecords
            );
        }


        /*
         * ============================================================
         * MASTER PRODUCT
         *
         * Example:
         *
         * Premium Cold Coffee
         *
         * master_product_id:
         * 22
         * 24
         * 25
         * 27
         *
         * All matching records will have category_id updated.
         * ============================================================
         */
        if (FmAppConstants.PRODUCT_TYPE_MASTER_PRODUCT
                .equals(productType)) {

            log.info(
                    "[UPDATE_CATEGORY_FOR_PRODUCT] Updating MASTERPRODUCT | productName={}",
                    productName
            );

            /*
             * First check whether Master Product exists.
             */
            List<FmMasterProductCategoryProjection> masterProducts =
                    productRepository
                            .findMasterProductCategoryDetails(
                                    productName
                            );

            if (masterProducts == null
                    || masterProducts.isEmpty()) {

                log.error(
                        "[UPDATE_CATEGORY_FOR_PRODUCT] Master Product not found | productName={}",
                        productName
                );

                throw new ResourceNotFoundException(
                        "Master Product not found with name : "
                                + productName
                );
            }

            /*
             * ========================================================
             * Update all matching master products
             * ========================================================
             */
            int updatedRecords =
                    productRepository
                            .updateMasterProductCategoryId(
                                    productName,
                                    updatedCategoryId
                            );

            if (updatedRecords == 0) {

                log.error(
                        "[UPDATE_CATEGORY_FOR_PRODUCT] Master Product category update failed | productName={}",
                        productName
                );

                throw new ResourceNotFoundException(
                        "Unable to update category for Master Product : "
                                + productName
                );
            }

            log.info(
                    "[UPDATE_CATEGORY_FOR_PRODUCT] MASTERPRODUCT category updated successfully | productName={} | updatedCategoryId={} | records={}",
                    productName,
                    updatedCategoryId,
                    updatedRecords
            );

            return FmProductMapper.mapCategoryUpdateResponse(
                    productType,
                    productName,
                    updatedCategoryId,
                    updatedRecords
            );
        }


        /*
         * ============================================================
         * INVALID PRODUCT TYPE
         * ============================================================
         */
        log.error(
                "[UPDATE_CATEGORY_FOR_PRODUCT] Invalid productType={}",
                productType
        );

        throw new IllegalArgumentException(
                "Invalid productType please enter valid product type :"
        );
    }
}