package com.jippy.foodandmart.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.foodandmart.dto.FmMapToProductRequest;
import com.jippy.foodandmart.dto.FmMapToProductResult;
import com.jippy.foodandmart.dto.FmMasterProductMappingResultDTO;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmProductMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for mapping master products into outlet-specific product tables.
 *
 * <p>Why a separate service: product mapping is a distinct business operation
 * (importing from the global master catalogue into a specific outlet's menu)
 * that sits outside the normal CRUD flow and deserves its own service.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmProductMappingServiceImpl implements IFmProductMappingService {

    private final FmProductRepository productRepository;
    private final FmProductVariantRepository productVariantRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmCategoryRepository categoryRepository;
    private final FmMasterProductRepository masterProductRepository;
    private final FmProductAvailableTimingRepository productAvailableTimingRepository;
    private final FmDaysOfWeekRepository daysOfWeekRepository;
    private final ObjectMapper                     objectMapper;

    /**
     * Maps a list of manually supplied product entries into the outlet's product table.
     *
     * <p>Why skip duplicates silently: when an admin re-runs the mapping for the
     * same outlet category, already-existing products should be skipped rather
     * than throwing an error or inserting duplicates.</p>
     *
     * <p>Why variants are saved separately: the products table holds the base
     * product while product_variants holds the per-size/per-option pricing.
     * A product is only marked hasProductVariants=true when at least one variant
     * entry is present in the request.</p>
     *
     * @param req the mapping request containing outletCategoryId and product entries
     * @return a {@link FmMapToProductResult} with counts of saved vs skipped products
     * @throws IllegalArgumentException if outletCategoryId is missing or category not found
     */
    @Override
    public FmMapToProductResult mapToProducts(FmMapToProductRequest req) {
        if (req.getProducts() == null || req.getProducts().isEmpty())
            throw new IllegalArgumentException("No products provided.");

        // Validate explicit outletCategoryId if provided
        if (req.getOutletCategoryId() != null) {
            outletCategoryRepository.findById(req.getOutletCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "OutletCategory ID " + req.getOutletCategoryId() + " not found."));
        }

        List<String> savedNames   = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();

        for (FmMapToProductRequest.ProductEntry entry : req.getProducts()) {
            String name = entry.getProductName() == null ? "" : entry.getProductName().trim();
            if (name.isBlank()) { skippedNames.add("(blank)"); continue; }

            // Resolve outletCategoryId: use explicit value or derive from master product's category
            Integer resolvedOutletCategoryId = req.getOutletCategoryId();

            if (resolvedOutletCategoryId == null) {
                // Derive category from the entry's categoryId or master product lookup
                Integer categoryId = entry.getCategoryId();
                if (categoryId == null && entry.getMasterProductId() != null) {
                    categoryId = masterProductRepository.findById(entry.getMasterProductId())
                            .map(FmMasterProduct::getCategoryId)
                            .orElse(null);
                }
                if (categoryId == null) {
                    log.warn("[MAP] Cannot resolve categoryId for product '{}' — skipping", name);
                    skippedNames.add(name + " (no category)");
                    continue;
                }
                if (req.getOutletId() == null) {
                    log.warn("[MAP] outletId missing for product '{}' — skipping", name);
                    skippedNames.add(name + " (no outlet)");
                    continue;
                }
                // Find or create outlet_categories row for this outlet + category
                final Integer finalCategoryId = categoryId;
                FmOutletCategory outletCategory = outletCategoryRepository
                        .findByOutletIdAndCategoryId(req.getOutletId(), categoryId)
                        .orElseGet(() -> {
                            FmOutletCategory oc = new FmOutletCategory();
                            oc.setOutletId(req.getOutletId());
                            oc.setCategoryId(finalCategoryId);
                            FmOutletCategory saved2 = outletCategoryRepository.save(oc);
                            log.info("[MAP] Created OutletCategory: outletId={} categoryId={} => outletCategoryId={}",
                                    req.getOutletId(), finalCategoryId, saved2.getOutletCategoryId());
                            return saved2;
                        });
                resolvedOutletCategoryId = outletCategory.getOutletCategoryId();
            }

            // Skip duplicate check
            if (productRepository.existsByOutletCategoryIdAndProductNameIgnoreCase(
                    resolvedOutletCategoryId, name)) {
                skippedNames.add(name);
                continue;
            }

            boolean hasVariants = Boolean.TRUE.equals(entry.getHasProductVariants())
                    && entry.getVariants() != null && !entry.getVariants().isEmpty();

            // Resolve images from master product if masterProductId is provided
            String imageLink = null;
            String photos    = null;
            String thumbnail = null;
            if (entry.getMasterProductId() != null) {
                FmMasterProduct mp = masterProductRepository.findById(entry.getMasterProductId())
                        .orElse(null);
                if (mp != null) {
                    imageLink = mp.getPhoto();
                    photos    = mp.getPhotos();
                    thumbnail = mp.getThumbnail();
                }
            }

            // IMAGE IS MANDATORY — throw error if master product has no image
            if (imageLink == null || imageLink.isBlank()) {
                throw new IllegalArgumentException(
                        "Product '" + name + "' cannot be added: no image found in master product. " +
                        "Please add an image to the master product first.");
            }

            // Create the base product record
            FmProduct product = new FmProduct();
            product.setOutletCategoryId(resolvedOutletCategoryId);
            product.setProductName(name);
            product.setDescription(entry.getDescription() == null ? "" : entry.getDescription());
            product.setIsVeg(entry.getIsVeg() == null ? Boolean.TRUE : entry.getIsVeg());
            product.setHasProductVariants(hasVariants);
            product.setImageLink(imageLink);
            product.setPhotos(photos);
            product.setThumbnail(thumbnail);
            // Use the price explicitly sent from the frontend (per-item or default).
            // Fall back to the CSV-derived static map only when nothing was provided.
            BigDecimal requestPrice = entry.getMerchantPrice();
            if (requestPrice != null && requestPrice.compareTo(BigDecimal.ZERO) > 0) {
                product.setMerchantPrice(requestPrice);
            } else {
                product.setMerchantPrice(resolvePrice(name));
            }
            FmProduct saved = productRepository.save(product);

            // Save availability timings from CSV or explicit timing entries
            saveTimings(saved.getProductId(), entry);

            // Save each variant if this product has options (e.g. Small/Medium/Large)
            if (hasVariants) {
                for (FmMapToProductRequest.VariantEntry ve : entry.getVariants()) {
                    if (ve.getVariantName() == null || ve.getVariantName().isBlank()) continue;
                    FmProductVariant variant = new FmProductVariant();
                    variant.setProductId(saved.getProductId());
                    variant.setVariantName(ve.getVariantName().trim());
                    variant.setMerchantPrice(ve.getMerchantPrice() == null ? BigDecimal.ZERO : ve.getMerchantPrice());
                    productVariantRepository.save(variant);
                }
            }
            savedNames.add(name);
        }

        log.info("[MAP] Done: saved={} skipped={}", savedNames.size(), skippedNames.size());
        FmMapToProductResult result = new FmMapToProductResult();
        result.setSavedCount(savedNames.size());
        result.setSkippedCount(skippedNames.size());
        result.setSavedNames(savedNames);
        result.setSkippedNames(skippedNames);
        return result;
    }

    /**
     * Maps all published master products from a global category into an outlet category.
     *
     * <p>Why filter by publish=1: unpublished master products are drafts or
     * discontinued items and should not appear in outlet menus.</p>
     *
     * <p>Why parse JSON options: master products may have a JSON options field
     * (e.g. {@code [{"name":"Small","price":99}]}) that represents product
     * variants. We parse this JSON and insert individual variant rows.</p>
     *
     * @param outletCategoryId the target outlet category to receive the products
     * @return a {@link FmMasterProductMappingResultDTO} with counts and names of saved vs skipped
     * @throws IllegalArgumentException if the outlet category or its linked category is not found
     */
    @Override
    public FmMasterProductMappingResultDTO mapFromMasterByCategory(Integer outletCategoryId) {
        FmOutletCategory outletCategory = outletCategoryRepository.findById(outletCategoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "OutletCategory ID " + outletCategoryId + " not found."));

        Integer categoryId = outletCategory.getCategoryId();
        FmCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category ID " + categoryId + " not found."));

        // Load all master products linked to this global category
        List<FmMasterProduct> masterProducts =
                masterProductRepository.findByCategoryIdOrderByMasterProductIdAsc(categoryId);

        log.info("[MASTER-MAP] outletCategoryId={} categoryId={} — {} master products found",
                outletCategoryId, categoryId, masterProducts.size());

        List<String> savedNames   = new ArrayList<>();
        List<String> skippedNames = new ArrayList<>();

        for (FmMasterProduct mp : masterProducts) {
            // Skip unpublished products — they are drafts not ready for outlet menus
            if (mp.getPublish() == null || mp.getPublish() != 1) {
                skippedNames.add(mp.getMasterProductName() + " (unpublished)");
                continue;
            }

            String name = mp.getMasterProductName().trim();
            // Skip if this product name already exists in the target outlet category
            if (productRepository.existsByOutletCategoryIdAndProductNameIgnoreCase(outletCategoryId, name)) {
                skippedNames.add(name);
                continue;
            }

            boolean hasVariants = mp.getHasOptions() != null && mp.getHasOptions() == 1
                    && mp.getOptions() != null && !mp.getOptions().isBlank();

            // IMAGE IS MANDATORY — skip with error message if master product has no image
            if (mp.getPhoto() == null || mp.getPhoto().isBlank()) {
                log.warn("[MASTER-MAP] Skipping '{}' (master_product_id={}) — no image found in master product",
                        name, mp.getMasterProductId());
                skippedNames.add(name + " (no image in master product)");
                continue;
            }

            FmProduct product = new FmProduct();
            product.setOutletCategoryId(outletCategoryId);
            product.setProductName(name);
            product.setDescription(mp.getDescription() == null ? "" : mp.getDescription());
            // Price defaults to zero at mapping time — outlet manager sets their selling price
            product.setMerchantPrice(BigDecimal.ZERO);
            product.setIsVeg(mp.getVeg() != null && mp.getVeg() == 1);
            product.setHasProductVariants(hasVariants);
            // Copy images from master product -> outlet product
            product.setImageLink(mp.getPhoto());
            product.setPhotos(mp.getPhotos());
            product.setThumbnail(mp.getThumbnail());
            FmProduct saved = productRepository.save(product);

            // Parse the JSON options field and create a ProductVariant row per option
            if (hasVariants) {
                try {
                    List<Map<String, Object>> optionsList = objectMapper.readValue(
                            mp.getOptions(), new TypeReference<List<Map<String, Object>>>() {});
                    for (Map<String, Object> opt : optionsList) {
                        String variantName = opt.getOrDefault("name", "").toString().trim();
                        if (variantName.isBlank()) continue;
                        BigDecimal price = BigDecimal.ZERO;
                        Object pv = opt.get("price");
                        if (pv != null) {
                            try { price = new BigDecimal(pv.toString()); }
                            catch (NumberFormatException ignored) {}
                        }
                        FmProductVariant variant = new FmProductVariant();
                        variant.setProductId(saved.getProductId());
                        variant.setVariantName(variantName);
                        variant.setMerchantPrice(price);
                        productVariantRepository.save(variant);
                    }
                } catch (JsonProcessingException e) {
                    log.warn("[MASTER-MAP] Could not parse options for master_product_id={}: {}",
                            mp.getMasterProductId(), e.getMessage());
                }
            }
            savedNames.add(name);
        }

        log.info("[MASTER-MAP] Done — saved={} skipped={}", savedNames.size(), skippedNames.size());
        FmMasterProductMappingResultDTO result = new FmMasterProductMappingResultDTO();
        result.setOutletCategoryId(outletCategoryId);
        result.setCategoryId(categoryId);
        result.setCategoryName(category.getCategoryName());
        result.setTotalMasterProducts(masterProducts.size());
        result.setSavedCount(savedNames.size());
        result.setSkippedCount(skippedNames.size());
        result.setSavedProductNames(savedNames);
        result.setSkippedProductNames(skippedNames);
        return result;
    }
    private BigDecimal resolvePrice(String productName) {
        Double price = FmProductMapper.priceMapper.get(productName);
        if (price == null || price < 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(price);
    }

    /**
     * Parses and inserts product_available_timings rows for the saved product.
     *
     * <p>Priority:
     * <ol>
     *   <li>If the request carries explicit {@code timings} list - use those.</li>
     *   <li>Otherwise parse {@code csvTiming} (comma-separated, e.g. "7:00-5:00,7:00-9:00")
     *       together with {@code csvDayOfWeek} to look up the real day_id from the
     *       days_of_week table.  One row is inserted per timing slot.</li>
     *   <li>Fall back to the static {@link FmProductMapper#timingMapper} and
     *       {@link FmProductMapper#dayOfWeekMapper} populated during the compare step.</li>
     * </ol>
     *
     * <p>Multiple timings for the same day are supported via comma separation,
     * e.g. "7:00-9:00,12:00-14:00" inserts two rows for the same day.</p>
     */
    private void saveTimings(Integer productId, FmMapToProductRequest.ProductEntry entry) {
        // --- explicit timing rows take priority ---
        if (entry.getTimings() != null && !entry.getTimings().isEmpty()) {
            for (FmMapToProductRequest.TimingEntry te : entry.getTimings()) {
                LocalTime start = parseTime(te.getStartTime());
                LocalTime end   = parseTime(te.getEndTime());
                if (start == null || end == null) continue;
                FmProductAvailableTiming pat = new FmProductAvailableTiming();
                pat.setProductId(productId);
                pat.setDayOfWeekId(te.getDayOfWeekId() != null ? te.getDayOfWeekId() : 0);
                pat.setStartTime(start);
                pat.setEndTime(end);
                productAvailableTimingRepository.save(pat);
            }
            return;
        }

        // --- fall back to csvTiming string / static map ---
        String rawTiming = entry.getCsvTiming();
        if (rawTiming == null || rawTiming.isBlank()) {
            rawTiming = FmProductMapper.timingMapper.get(entry.getProductName());
        }
        if (rawTiming == null || rawTiming.isBlank()) return;

        // Resolve the day_of_week_id from the days_of_week table using the day name
        String dayName = entry.getCsvDayOfWeek();
        if (dayName == null || dayName.isBlank()) {
            dayName = FmProductMapper.dayOfWeekMapper.get(entry.getProductName());
        }
        Integer dayId = 0; // default: all days
        if (dayName != null && !dayName.isBlank()) {
            String finalDayName = dayName.trim();
            dayId = daysOfWeekRepository.findByDayNameIgnoreCase(finalDayName)
                    .map(d -> d.getDayId())
                    .orElseGet(() -> {
                        log.warn("[MAP] Day name '{}' not found in days_of_week table, using 0 (all days)", finalDayName);
                        return 0;
                    });
        }

        // Support multiple comma-separated time slots, e.g. "7:00-5:00,7:00-9:00"
        String[] slots = rawTiming.trim().split(",");
        for (String slot : slots) {
            slot = slot.trim();
            String[] parts = slot.split("-", 2);
            if (parts.length != 2) {
                log.warn("[MAP] Cannot parse timing slot '{}' for productId={}", slot, productId);
                continue;
            }
            LocalTime start = parseTime(parts[0].trim());
            LocalTime end   = parseTime(parts[1].trim());
            if (start == null || end == null) {
                log.warn("[MAP] Invalid time in timing slot '{}' for productId={}", slot, productId);
                continue;
            }
            FmProductAvailableTiming pat = new FmProductAvailableTiming();
            pat.setProductId(productId);
            pat.setDayOfWeekId(dayId);
            pat.setStartTime(start);
            pat.setEndTime(end);
            productAvailableTimingRepository.save(pat);
            log.info("[MAP] Saved timing productId={} dayOfWeekId={} dayName='{}' start={} end={}", productId, dayId, dayName, start, end);
        }
    }

    /** Parses "H:mm" or "HH:mm" safely; returns null on failure. */
    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            // Pad single-digit hours: "7:00" → "07:00"
            String padded = raw.trim();
            if (padded.indexOf(':') == 1) padded = "0" + padded;
            return LocalTime.parse(padded, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            log.warn("[MAP] Could not parse time '{}': {}", raw, e.getMessage());
            return null;
        }
    }
}
