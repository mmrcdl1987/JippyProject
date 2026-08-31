

package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.entity.FmMasterProduct;
import com.jippy.foodandmart.exception.*;
import com.jippy.foodandmart.mapper.FmCreateMasterProductMapper;
import com.jippy.foodandmart.mapper.FmMasterProductMapper;
import com.jippy.foodandmart.mapper.FmProductMapper;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.repository.FmMasterProductRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FmMasterProductService {

    private final FmMasterProductRepository masterProductRepository;
    private final FileConverterService fileConverterService;
    private final FmCategoryRepository categoryRepository;
    private final FmMasterProductMapper masterProductMapper;
    private final FmCreateMasterProductMapper mapper;

    // ============================================================
    // CREATE
    // ============================================================

    public FmMasterProduct save(FmMasterProductRequest req) {

        FmMasterProductMapper.validateForCreate(req);

        /*
         * PHOTO IS OPTIONAL.
         *
         * No photo validation is performed here.
         * Product can be created without photo.
         */

        FmMasterProduct entity = FmMasterProductMapper.toEntity(req);

        FmMasterProduct saved = masterProductRepository.save(entity);

        log.info("[MASTER] Saved id={} name={} productType={}", saved.getMasterProductId(), saved.getMasterProductName(), saved.getProductType());

        return saved;
    }

    // ============================================================
    // BULK CREATE
    // ============================================================

    @Transactional
    public List<FmMasterProduct> saveAll(List<FmMasterProductRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Product list cannot be null or empty.");
        }

        List<FmMasterProduct> toInsert = new ArrayList<>();

        for (FmMasterProductRequest req : requests) {

            if (req == null) {
                throw new BadRequestException("Product request cannot be null.");
            }

            // ====================================================
            // PRODUCT NAME
            // ====================================================

            if (isBlank(req.getMasterProductName())) {
                throw new BadRequestException("Master product name is required.");
            }

            String productName = req.getMasterProductName().trim();

            // ====================================================
            // CATEGORY NAME
            // ====================================================

            if (isBlank(req.getCategoryName())) {
                throw new BadRequestException("Category name is required for product: " + productName);
            }

            String categoryName = req.getCategoryName().trim();

            // ====================================================
            // VEG / NON VEG
            // ====================================================

            Integer veg = req.getVeg();
            Integer nonVeg = req.getNonVeg();

            boolean isVeg = veg != null && veg == 1;

            boolean isNonVeg = nonVeg != null && nonVeg == 1;

            if ((isVeg && isNonVeg) || (!isVeg && !isNonVeg)) {

                throw new BadRequestException("Select either Veg or Non-Veg " + "(not both or neither) for product: " + productName);
            }

            // ====================================================
            // DUPLICATE CHECK
            //
            // IMPORTANT:
            // Product Name + Category Name
            //
            // DO NOT use categoryId from CSV/request here.
            // ====================================================

            boolean alreadyExists = masterProductRepository.existsByMasterProductNameIgnoreCaseAndCategoryNameIgnoreCase(productName, categoryName);

            if (alreadyExists) {

                log.info("[MASTER] Duplicate skipped: product={} category={}", productName, categoryName);

                continue;
            }

            // ====================================================
            // FIND OR CREATE CATEGORY
            // ====================================================

            FmCategory category = categoryRepository.findByCategoryNameIgnoreCase(categoryName).orElseGet(() -> {

                log.info("[CATEGORY] Category not found. Creating: {}", categoryName);

                FmCategory newCategory = new FmCategory();

                newCategory.setCategoryName(categoryName);

                return categoryRepository.save(newCategory);
            });

            // ====================================================
            // GET CATEGORY ID
            // ====================================================

            Integer categoryId = category.getCategoryId();

            log.info("[CATEGORY] Resolved category: name={} id={}", category.getCategoryName(), categoryId);

            // ====================================================
            // SET RESOLVED CATEGORY
            // ====================================================

            req.setCategoryId(categoryId);

            req.setCategoryName(category.getCategoryName());

            // ====================================================
            // CREATE MASTER PRODUCT
            // ====================================================

            FmMasterProduct entity = FmMasterProductMapper.toEntity(req);

            // Explicitly set values from the category table.
            entity.setCategoryId(categoryId);

            entity.setCategoryName(category.getCategoryName());

            toInsert.add(entity);

            log.info("[MASTER] Prepared insert: product={} category={} categoryId={}", productName, category.getCategoryName(), categoryId);
        }

        // ========================================================
        // NOTHING TO INSERT
        // ========================================================

        if (toInsert.isEmpty()) {

            log.info("[MASTER] Bulk insert: 0/{} - no new products", requests.size());

            return Collections.emptyList();
        }

        // ========================================================
        // SAVE MASTER PRODUCTS
        // ========================================================

        List<FmMasterProduct> saved = masterProductRepository.saveAll(toInsert);

        log.info("[MASTER] Bulk insert completed: {}/{}", saved.size(), requests.size());

        return saved;
    }

    // ============================================================
    // READ
    // ============================================================

    @Transactional(readOnly = true)
    public Page<FmMasterProduct> getAll(Pageable pageable) {

        return masterProductRepository.findAll(pageable);
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public FmMasterProduct getById(Integer id) {

        return masterProductRepository.findById(id).orElseThrow(() -> new MasterProductNotFoundException(id));
    }

    // ============================================================
    // FILTER
    // ============================================================

    @Transactional(readOnly = true)
    public List<FmMasterProduct> filter(String type) {

        String normalised = FmMasterProductMapper.validateType(type);

        return masterProductRepository.filterByType(normalised);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Transactional(readOnly = true)
    public List<FmMasterProduct> search(String keyword) {

        String kw = FmMasterProductMapper.validateSearchKeyword(keyword);

        return masterProductRepository.searchByName(kw);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Transactional
    public FmMasterProduct update(Integer id, FmMasterProductRequest req) {

        FmMasterProduct existing = masterProductRepository.findById(id).orElseThrow(() -> new MasterProductNotFoundException(id));

        boolean isNewCategory = (req.getCategoryId() == null || req.getCategoryId() == 0) && req.getCategoryName() != null && !req.getCategoryName().trim().isEmpty();

        if (isNewCategory) {

            String newCatName = req.getCategoryName().trim();

            FmCategory category = categoryRepository.findByCategoryNameIgnoreCase(newCatName).orElseGet(() -> {

                log.info("[CATEGORY] Creating new category in DB: {}", newCatName);

                FmCategory newCategory = new FmCategory();

                newCategory.setCategoryName(newCatName);

                return categoryRepository.save(newCategory);
            });

            req.setCategoryId(category.getCategoryId());

            req.setCategoryName(category.getCategoryName());
        }

        FmMasterProductMapper.validateForUpdate(req);

        /*
         * Product type is updated by mapper.
         *
         * Photo is NOT validated here.
         */
        FmMasterProductMapper.updateEntity(existing, req);

        FmMasterProduct saved = masterProductRepository.save(existing);

        log.info("[MASTER] Successfully updated product " + "id={} categoryId={} productType={}", saved.getMasterProductId(), saved.getCategoryId(), saved.getProductType());

        return saved;
    }

    // ============================================================
    // DELETE
    // ============================================================

    public void delete(Integer id) {

        if (!masterProductRepository.existsById(id)) {

            throw new MasterProductNotFoundException(id);
        }

        masterProductRepository.deleteById(id);

        log.info("[MASTER] Deleted id={}", id);
    }

    // ============================================================
    // PHOTO UPLOAD
    // ============================================================

    /**
     * Photo validation is performed ONLY when
     * merchant explicitly uploads a photo.
     * <p>
     * Product creation/add-to-outlet does NOT require photo.
     */
    public String updatePhoto(Integer id, MultipartFile photo) {

        if (photo == null || photo.isEmpty()) {

            throw new IllegalArgumentException("Photo file cannot be empty.");
        }

        /*
         * Photo validation remains here because
         * this method is specifically for photo upload.
         */
        FmMasterProductMapper.validatePhoto(photo.getContentType(), photo.getSize());

        try {

            byte[] bytes = photo.getBytes();

            String base64 = Base64.getEncoder().encodeToString(bytes);

            String uri = "data:" + photo.getContentType() + ";base64," + base64;

            FmMasterProduct mp = masterProductRepository.findById(id).orElseThrow(() -> new MasterProductNotFoundException(id));

            mp.setPhoto(uri);

            masterProductRepository.save(mp);

            log.info("[MASTER] Photo saved id={}", id);

            return uri;

        } catch (MasterProductNotFoundException e) {

            throw e;

        } catch (Exception e) {

            throw new FileProcessingException("Failed to store photo: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // COMPARE CSV FILE WITH DATABASE
    // ============================================================

    public FmCompareFileResponse compareFileWithDB(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException("File is null or empty.");
        }

        if (file.getSize() > 10 * 1024 * 1024L) {

            throw new IllegalArgumentException("File exceeds 10 MB size limit.");
        }

        byte[] fileBytes;

        try {

            fileBytes = file.getBytes();

        } catch (Exception e) {

            throw new FileProcessingException("Failed to read file bytes: " + e.getMessage(), e);
        }

        InputStream csvStream = fileConverterService.convertToCsvFromBytes(fileBytes, file.getOriginalFilename());

        List<FmMasterProduct> parsed = parseCsv(csvStream);

        if (parsed.isEmpty()) {

            return new FmCompareFileResponse(List.of(), List.of(), 0, 0, 0, 0);
        }

        List<FmMasterProduct> db = masterProductRepository.findAllByOrderByMasterProductIdAsc();

        Map<String, FmMasterProduct> dbLookup = new HashMap<>();

        for (FmMasterProduct d : db) {

            if (!isBlank(d.getMasterProductName()) && !isBlank(d.getCategoryName())) {

                String key = norm(d.getMasterProductName()) + "|" + norm(d.getCategoryName());

                dbLookup.put(key, d);
            }
        }

        List<FmCompareFileResponse.CompareItem> dupes = new ArrayList<>();

        List<FmCompareFileResponse.CompareItem> newOnes = new ArrayList<>();

        int skipped = 0;

        for (FmMasterProduct fp : parsed) {

            // ----------------------------------------------------
            // PRODUCT NAME
            // ----------------------------------------------------

            if (isBlank(fp.getMasterProductName())) {

                log.warn("[MASTER] Skipping CSV row: " + "Missing Master Product Name.");

                skipped++;
                continue;
            }

            // ----------------------------------------------------
            // CATEGORY NAME
            // ----------------------------------------------------

            if (isBlank(fp.getCategoryName())) {

                log.warn("[MASTER] Skipping CSV row: " + "Missing Category Name for product '{}'.", fp.getMasterProductName());

                skipped++;
                continue;
            }

            // ----------------------------------------------------
            // VEG / NON VEG
            // ----------------------------------------------------

            int veg = fp.getVeg() != null ? fp.getVeg() : 0;

            int nonVeg = fp.getNonVeg() != null ? fp.getNonVeg() : 0;

            if ((veg == 1 && nonVeg == 1) || (veg == 0 && nonVeg == 0)) {

                log.warn("[MASTER] Skipping CSV row: " + "Invalid Veg/NonVeg configuration " + "for product '{}' (veg={}, nonVeg={}).", fp.getMasterProductName(), veg, nonVeg);

                skipped++;
                continue;
            }

            // ----------------------------------------------------
            // DUPLICATE CHECK
            // ----------------------------------------------------

            String key = norm(fp.getMasterProductName()) + "|" + norm(fp.getCategoryName());

            FmMasterProduct dbMatch = dbLookup.get(key);

            if (dbMatch != null) {

                dupes.add(toCompareItem(dbMatch.getMasterProductId(), dbMatch, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));

            } else {

                newOnes.add(toCompareItem(null, fp, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));
            }
        }

        log.info("[MASTER] Compare: dup={} new={} skipped={}", dupes.size(), newOnes.size(), skipped);

        return new FmCompareFileResponse(dupes, newOnes, parsed.size(), dupes.size(), newOnes.size(), skipped);
    }

    // ============================================================
    // CREATE COMPARE ITEM
    // ============================================================

    private FmCompareFileResponse.CompareItem toCompareItem(Integer id, FmMasterProduct mp, Double csvPrice, String csvTiming, String csvDayOfWeek) {

        log.info("[MASTER] toCompareItem: " + "product='{}' " + "id={} " + "productType={} " + "csvPrice={} " + "csvTiming={} " + "csvDayOfWeek={}", mp.getMasterProductName(), id, mp.getProductType(), csvPrice, csvTiming, csvDayOfWeek);

        /*
         * productType has been added after grams.
         *
         * FmCompareFileResponse.CompareItem must have
         * the same constructor order.
         */
        return new FmCompareFileResponse.CompareItem(id, mp.getMasterProductName(),

                mp.getVeg(), mp.getNonVeg(),

                mp.getCategoryId(), mp.getCategoryName(),

                mp.getSubCategoryId(), mp.getSubCategoryName(),

                mp.getDescription(), mp.getShortDescription(),

                mp.getPhoto(), mp.getPhotos(), mp.getThumbnail(),

                mp.getFoodType(), mp.getCuisineType(),

                mp.getHasOptions(), mp.getOptionsEnabled(),

                mp.getOptions(),

                mp.getCalories(), mp.getProtein(), mp.getFats(), mp.getCarbs(), mp.getGrams(),

                // NEW
                mp.getProductType(),

                mp.getPublish(),

                csvPrice, csvTiming, csvDayOfWeek);
    }

    // ============================================================
    // CSV PARSER
    // ============================================================

    private List<FmMasterProduct> parseCsv(InputStream stream) {

        List<FmMasterProduct> list = new ArrayList<>();

        FmProductMapper.priceMapper.clear();
        FmProductMapper.timingMapper.clear();
        FmProductMapper.dayOfWeekMapper.clear();

        try (CSVReader r = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String[] row;

            boolean header = true;

            int nameIdx = -1;
            int descIdx = -1;
            int shortDescIdx = -1;

            int vegIdx = -1;
            int nonVegIdx = -1;

            int categoryIdIdx = -1;
            int categoryNameIdx = -1;

            int photoIdx = -1;
            int photosIdx = -1;
            int thumbnailIdx = -1;

            int foodTypeIdx = -1;
            int cuisineTypeIdx = -1;

            int hasOptionsIdx = -1;
            int optionsEnabledIdx = -1;
            int optionsIdx = -1;

            int publishIdx = -1;

            int caloriesIdx = -1;
            int proteinsIdx = -1;
            int fatsIdx = -1;
            int carbsIdx = -1;
            int gramsIdx = -1;

            /*
             * NEW COLUMN
             */
            int productTypeIdx = -1;

            int priceIdx = -1;
            int timingIdx = -1;
            int dayOfWeekIdx = -1;

            while ((row = r.readNext()) != null) {

                // =================================================
                // HEADER
                // =================================================

                if (header) {

                    header = false;

                    log.info("[MASTER] CSV headers detected: {}", Arrays.toString(row));

                    for (int i = 0; i < row.length; i++) {

                        String h = normalizeHeader(row[i]);

                        // -----------------------------------------
                        // PRODUCT NAME
                        // -----------------------------------------

                        if ("master_product_name".equals(h) || "masterproductname".equals(h) || "name".equals(h)) {

                            nameIdx = i;

                            log.info("[MASTER] Master Product Name column detected at index {}: '{}'", i, h);
                        }

                        // -----------------------------------------
                        // DESCRIPTION
                        // -----------------------------------------

                        if ("description".equals(h)) {

                            descIdx = i;
                        }

                        // -----------------------------------------
                        // SHORT DESCRIPTION
                        // -----------------------------------------

                        if ("short_description".equals(h) || "shortdescription".equals(h)) {

                            shortDescIdx = i;
                        }

                        // -----------------------------------------
                        // VEG
                        // -----------------------------------------

                        if ("veg".equals(h)) {

                            vegIdx = i;
                        }

                        // -----------------------------------------
                        // NON VEG
                        // -----------------------------------------

                        if ("nonveg".equals(h) || "non_veg".equals(h)) {

                            nonVegIdx = i;
                        }

                        // -----------------------------------------
                        // CATEGORY ID
                        // -----------------------------------------

                        if ("categoryid".equals(h) || "category_id".equals(h)) {

                            categoryIdIdx = i;
                        }

                        // -----------------------------------------
                        // CATEGORY NAME
                        // -----------------------------------------

                        if ("categoryname".equals(h) || "category_name".equals(h) || "category".equals(h) || "categorytitle".equals(h) || "category_title".equals(h)) {

                            categoryNameIdx = i;
                        }

                        // -----------------------------------------
                        // PHOTO
                        // -----------------------------------------

                        if ("photo".equals(h)) {

                            photoIdx = i;
                        }

                        // -----------------------------------------
                        // PHOTOS
                        // -----------------------------------------

                        if ("photos".equals(h)) {

                            photosIdx = i;
                        }

                        // -----------------------------------------
                        // THUMBNAIL
                        // -----------------------------------------

                        if ("thumbnail".equals(h)) {

                            thumbnailIdx = i;
                        }

                        // -----------------------------------------
                        // FOOD TYPE
                        // -----------------------------------------

                        if ("food_type".equals(h) || "foodtype".equals(h)) {

                            foodTypeIdx = i;
                        }

                        // -----------------------------------------
                        // CUISINE TYPE
                        // -----------------------------------------

                        if ("cuisine_type".equals(h) || "cuisinetype".equals(h)) {

                            cuisineTypeIdx = i;
                        }

                        // -----------------------------------------
                        // HAS OPTIONS
                        // -----------------------------------------

                        if ("has_options".equals(h) || "hasoptions".equals(h)) {

                            hasOptionsIdx = i;
                        }

                        // -----------------------------------------
                        // OPTIONS ENABLED
                        // -----------------------------------------

                        if ("options_enabled".equals(h) || "optionsenabled".equals(h)) {

                            optionsEnabledIdx = i;
                        }

                        // -----------------------------------------
                        // OPTIONS
                        // -----------------------------------------

                        if ("options".equals(h)) {

                            optionsIdx = i;
                        }

                        // -----------------------------------------
                        // PUBLISH
                        // -----------------------------------------

                        if ("publish".equals(h)) {

                            publishIdx = i;
                        }

                        // -----------------------------------------
                        // CALORIES
                        // -----------------------------------------

                        if ("calories".equals(h)) {

                            caloriesIdx = i;
                        }

                        // -----------------------------------------
                        // PROTEIN
                        // -----------------------------------------

                        if ("proteins".equals(h) || "protein".equals(h)) {

                            proteinsIdx = i;
                        }

                        // -----------------------------------------
                        // FATS
                        // -----------------------------------------

                        if ("fats".equals(h)) {

                            fatsIdx = i;
                        }

                        // -----------------------------------------
                        // CARBS
                        // -----------------------------------------

                        if ("carbs".equals(h)) {

                            carbsIdx = i;
                        }

                        // -----------------------------------------
                        // GRAMS
                        // -----------------------------------------

                        if ("grams".equals(h)) {

                            gramsIdx = i;
                        }

                        // -----------------------------------------
                        // PRODUCT TYPE - NEW
                        // -----------------------------------------

                        if ("product_type".equals(h) || "producttype".equals(h)) {

                            productTypeIdx = i;

                            log.info("[MASTER] Product Type column " + "detected at index {}: '{}'", i, h);
                        }

                        // -----------------------------------------
                        // PRICE
                        // -----------------------------------------

                        if (h.contains("price") || h.contains("pric") || h.equals("mrp") || h.contains("merchant") || h.contains("merchat")) {

                            priceIdx = i;

                            log.info("[MASTER] Price column detected " + "at index {}: '{}'", i, h);
                        }

                        // -----------------------------------------
                        // TIMING
                        // -----------------------------------------

                        if (h.contains("timing") || h.contains("time") || h.contains("avail") || h.contains("avelabule")) {

                            timingIdx = i;

                            log.info("[MASTER] Timing column detected " + "at index {}: '{}'", i, h);
                        }

                        // -----------------------------------------
                        // DAY OF WEEK
                        // -----------------------------------------

                        if (h.contains("dayofaweek") || h.contains("daysofaweek") || h.contains("daysofweek") || h.contains("weekday") || h.equals("day") || h.equals("days")) {

                            dayOfWeekIdx = i;

                            log.info("[MASTER] DayOfWeek column detected " + "at index {}: '{}'", i, h);
                        }
                    }

                    if (nameIdx < 0) {

                        throw new IllegalArgumentException("Required CSV column 'master_product_name' was not found.");
                    }

                    log.info("[MASTER] Required CSV column validation completed. " + "master_product_name index={}", nameIdx);

                    continue;
                }

                // =================================================
                // DATA ROW
                // =================================================

                FmMasterProduct mp = new FmMasterProduct();

                // -----------------------------------------------
                // BASIC INFORMATION
                // -----------------------------------------------

                mp.setMasterProductName(safeGet(row, nameIdx));

                mp.setDescription(safeGet(row, descIdx));

                mp.setShortDescription(safeGet(row, shortDescIdx));

                /*
                 * Photo is read if available,
                 * but it is NOT required.
                 */
                mp.setPhoto(safeGet(row, photoIdx));

                mp.setPhotos(safeGetRaw(row, photosIdx));

                mp.setThumbnail(safeGet(row, thumbnailIdx));

                // -----------------------------------------------
                // FOOD INFORMATION
                // -----------------------------------------------

                mp.setFoodType(safeGet(row, foodTypeIdx));

                mp.setCuisineType(safeGet(row, cuisineTypeIdx));

                mp.setOptions(safeGetRaw(row, optionsIdx));

                // -----------------------------------------------
                // VEG / NON VEG
                // -----------------------------------------------

                String v = norm(safeGet(row, vegIdx));

                String nv = norm(safeGet(row, nonVegIdx));

                mp.setVeg("1".equals(v) || "true".equals(v) ? 1 : 0);

                mp.setNonVeg("1".equals(nv) || "true".equals(nv) ? 1 : 0);

                // -----------------------------------------------
                // OPTIONS
                // -----------------------------------------------

                mp.setHasOptions(parseIntSafe(safeGet(row, hasOptionsIdx)));

                mp.setOptionsEnabled(parseIntSafe(safeGet(row, optionsEnabledIdx)));

                // -----------------------------------------------
                // PUBLISH
                // -----------------------------------------------

                mp.setPublish(parseIntSafe(safeGet(row, publishIdx), 1));

                // -----------------------------------------------
                // NUTRITION
                // -----------------------------------------------

                mp.setCalories(parseIntSafe(safeGet(row, caloriesIdx)));

                mp.setProtein(parseIntSafe(safeGet(row, proteinsIdx)));

                mp.setFats(parseIntSafe(safeGet(row, fatsIdx)));

                mp.setCarbs(parseIntSafe(safeGet(row, carbsIdx)));

                mp.setGrams(parseIntSafe(safeGet(row, gramsIdx)));

                // -----------------------------------------------
                // PRODUCT TYPE - NEW
                // -----------------------------------------------

                String productType = safeGet(row, productTypeIdx);

                if (!isBlank(productType)) {

                    productType = productType.trim();
                }

                mp.setProductType(productType);

                // -----------------------------------------------
                // CATEGORY
                // -----------------------------------------------

                String catIdStr = safeGet(row, categoryIdIdx);

                String catName = safeGet(row, categoryNameIdx);

                mp.setCategoryName(catName);

                // -----------------------------------------------
                // MERCHANT PRICE
                // -----------------------------------------------

                String rawPrice = safeGet(row, priceIdx);

                double csvPrice = 0.0;

                if (!isBlank(rawPrice)) {

                    String cleanPrice = rawPrice.replaceAll("[^0-9.]", "").trim();

                    try {

                        if (!cleanPrice.isBlank()) {

                            csvPrice = Double.parseDouble(cleanPrice);
                        }

                    } catch (NumberFormatException ignored) {

                        log.warn("[MASTER] Could not parse price " + "rawPrice='{}' " + "cleanPrice='{}' " + "for product='{}'", rawPrice, cleanPrice, mp.getMasterProductName());
                    }
                }

                mp.setCsvMerchantPrice(csvPrice);

                FmProductMapper.priceMapper.put(safeGet(row, nameIdx), csvPrice);

                // -----------------------------------------------
                // TIMING
                // -----------------------------------------------

                String csvTiming = safeGet(row, timingIdx);

                if (isBlank(csvTiming)) {

                    csvTiming = null;
                }

                mp.setCsvTiming(csvTiming);

                FmProductMapper.timingMapper.put(safeGet(row, nameIdx), csvTiming);

                // -----------------------------------------------
                // DAY OF WEEK
                // -----------------------------------------------

                String csvDayOfWeek = safeGet(row, dayOfWeekIdx);

                if (isBlank(csvDayOfWeek)) {

                    csvDayOfWeek = null;
                }

                mp.setCsvDayOfWeek(csvDayOfWeek);

                FmProductMapper.dayOfWeekMapper.put(safeGet(row, nameIdx), csvDayOfWeek);

                // -----------------------------------------------
                // CATEGORY ID
                // -----------------------------------------------

                if (!isBlank(catIdStr)) {

                    try {

                        mp.setCategoryId(Integer.parseInt(catIdStr.trim()));

                    } catch (NumberFormatException ignored) {

                        mp.setCategoryId(resolveOrCreateCategoryId(catName));
                    }

                } else if (!isBlank(catName)) {

                    mp.setCategoryId(resolveOrCreateCategoryId(catName));
                }

                // -----------------------------------------------
                // ADD TO LIST
                // -----------------------------------------------

                list.add(mp);
            }

        } catch (Exception e) {

            throw new FileProcessingException("CSV parse error: " + e.getMessage(), e);
        }

        log.info("[MASTER] CSV parsing completed. Total rows={}", list.size());

        return list;
    }

    // ============================================================
    // INTEGER PARSER
    // ============================================================

    private int parseIntSafe(String val) {

        return parseIntSafe(val, 0);
    }

    private int parseIntSafe(String val, int defaultVal) {

        if (isBlank(val)) {

            return defaultVal;
        }

        try {

            return (int) Double.parseDouble(val.trim());

        } catch (NumberFormatException e) {

            return defaultVal;
        }
    }

    // ============================================================
    // CATEGORY RESOLUTION
    // ============================================================

    private Integer resolveOrCreateCategoryId(String catName) {

        if (isBlank(catName)) {

            return null;
        }

        FmCategory cat = categoryRepository.findByCategoryNameIgnoreCase(catName.trim()).orElseGet(() -> {

            FmCategory newCat = new FmCategory();

            newCat.setCategoryName(catName.trim());

            return categoryRepository.save(newCat);
        });

        return cat.getCategoryId();
    }

    // ============================================================
    // SAFE CSV GET
    // ============================================================

    private String safeGet(String[] row, int idx) {

        if (idx < 0 || idx >= row.length) {

            return null;
        }

        return row[idx] == null ? null : row[idx].replace("\"", "").replace("\r", "").trim();
    }

    // ============================================================
    // SAFE RAW CSV GET
    // ============================================================

    private String safeGetRaw(String[] row, int idx) {

        if (idx < 0 || idx >= row.length) {

            return null;
        }

        String val = row[idx];

        if (val == null) {

            return null;
        }

        val = val.replace("\r", "").trim();

        return val.isEmpty() ? null : val;
    }

    // ============================================================
    // NORMALIZE
    // ============================================================

    private String norm(String v) {

        if (v == null) {

            return "";
        }

        return v.replace("\"", "").replace("\r", "").replace("\n", "").trim().toLowerCase().replaceAll("\\s+", " ");
    }

    // ============================================================
    // NORMALIZE CSV HEADER
    // ============================================================

    /**
     * Normalizes CSV headers independently from product-name
     * comparison normalization.
     * <p>
     * Supports headers such as:
     * master_product_name
     * masterproductname
     * MASTER_PRODUCT_NAME
     * <p>
     * Also removes UTF-8 BOM if present.
     */
    private String normalizeHeader(String value) {

        if (value == null) {

            return "";
        }

        return value.replace("\uFEFF", "").replace("\"", "").replace("\r", "").replace("\n", "").trim().toLowerCase();
    }

    // ============================================================
    // BLANK CHECK
    // ============================================================

    private boolean isBlank(String str) {

        return str == null || str.isBlank();
    }

    // ============================================================
    // GET PRODUCTS BY CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public List<FmMasterProductResponseDto> getProductsByCategory(Integer categoryId, String keyword) {

        log.info("GET_PRODUCTS_BY_CATEGORY_STARTED | " + "categoryId={} | keyword={}", categoryId, keyword);

        if (!categoryRepository.existsById(categoryId)) {

            log.warn("CATEGORY_NOT_FOUND | categoryId={}", categoryId);

            throw new ResourceNotFoundException("Category not found with id : " + categoryId);
        }

        /*
         * IMPORTANT:
         *
         * This method is used when merchant selects
         * products while adding products to an outlet.
         *
         * PHOTO IS NOT REQUIRED.
         *
         * Products without photos are returned normally.
         */
        List<FmMasterProduct> products = masterProductRepository.findProductsByCategoryAndKeyword(categoryId, keyword);

        log.info("GET_PRODUCTS_BY_CATEGORY_COMPLETED | " + "categoryId={} | productCount={}", categoryId, products.size());

        return products.stream().map(masterProductMapper::toResponseDto).toList();
    }

    // ============================================================
    // CREATE MASTER PRODUCT
    // ============================================================

    @Transactional
    public FmCreateMasterProductResponseDto createMasterProduct(FmCreateMasterProductRequestDto request) {

        log.info("CREATE_MASTER_PRODUCT_STARTED | " + "categoryId={} | productName={}", request.getCategoryId(), request.getMasterProductName());

        FmCreateMasterProductMapper.validate(request);

        request.setMasterProductName(request.getMasterProductName().trim());

        if (request.getDescription() != null) {

            request.setDescription(request.getDescription().trim());
        }

        if (request.getShortDescription() != null) {

            request.setShortDescription(request.getShortDescription().trim());
        }

        if (request.getFoodType() != null) {

            request.setFoodType(request.getFoodType().trim().toUpperCase());
        }

        if (request.getCuisineType() != null) {

            request.setCuisineType(request.getCuisineType().trim());
        }

        /*
         * NO PHOTO VALIDATION HERE.
         *
         * Master product can be created without photo.
         */

        FmCategory category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id : " + request.getCategoryId()));

        // --------------------------------------------------------
        // FOOD TYPE VALIDATION
        // --------------------------------------------------------

        if (request.getFoodType() != null) {

            if (request.getIsVeg() && !"VEG".equalsIgnoreCase(request.getFoodType())) {

                throw new BadRequestException("Veg products must have food type as VEG.");
            }

            if (!request.getIsVeg() && "VEG".equalsIgnoreCase(request.getFoodType())) {

                throw new BadRequestException("Non Veg products cannot have food type as VEG.");
            }
        }

        // --------------------------------------------------------
        // DUPLICATE CHECK
        // --------------------------------------------------------

        if (masterProductRepository.existsByMasterProductNameIgnoreCaseAndCategoryId(request.getMasterProductName(), request.getCategoryId())) {

            throw new DuplicateResourceException("Master Product already exists in this category.");
        }

        // --------------------------------------------------------
        // CREATE ENTITY
        // --------------------------------------------------------

        FmMasterProduct entity = FmCreateMasterProductMapper.toEntity(request, category.getCategoryName(), 1);

        /*
         * No photo validation.
         */
        FmMasterProduct savedProduct = masterProductRepository.save(entity);

        log.info("CREATE_MASTER_PRODUCT_COMPLETED | " + "masterProductId={} | productType={}", savedProduct.getMasterProductId(), savedProduct.getProductType());

        return mapper.toResponseDto(savedProduct);
    }
}

