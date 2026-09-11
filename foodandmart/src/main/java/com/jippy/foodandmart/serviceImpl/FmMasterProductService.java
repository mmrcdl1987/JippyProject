package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmCompareFileResponse;
import com.jippy.foodandmart.dto.FmCreateMasterProductRequestDto;
import com.jippy.foodandmart.dto.FmCreateMasterProductResponseDto;
import com.jippy.foodandmart.dto.FmMasterProductRequest;
import com.jippy.foodandmart.dto.FmMasterProductResponseDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.entity.FmMasterProduct;
import com.jippy.foodandmart.exception.BadRequestException;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.FileProcessingException;
import com.jippy.foodandmart.exception.MasterProductNotFoundException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmCreateMasterProductMapper;
import com.jippy.foodandmart.mapper.FmMasterProductMapper;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.repository.FmMasterProductRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Service for Master Product operations.
 * <p>
 * IMPORTANT:
 * <p>
 * 1. Bulk upload values come from Excel/CSV.
 * 2. No product-specific values are hard-coded.
 * 3. category_id is optional in Excel/CSV.
 * category_name is used to find/create the category.
 * 4. Veg/Non-Veg uses ONLY:
 * <p>
 * is_veg = true
 * is_veg = false
 * <p>
 * 5. CSV-only fields:
 * <p>
 * merchant_price
 * timing
 * daysofaweek
 * <p>
 * are NOT stored in master_products.
 * They are preserved temporarily and returned
 * through CompareItem for the outlet pricing /
 * availability flow.
 */
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

        if (req == null) {
            throw new BadRequestException("Master product request cannot be null.");
        }

        validateRequest(req);

        FmMasterProduct entity = new FmMasterProduct();

        mapRequestToEntity(req, entity);

        /*
         * System generated fields.
         */
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(req.getCreatedBy());
        }

        if (isBlank(entity.getIsActive())) {
            entity.setIsActive("Y");
        }

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

        /*
         * Prevent duplicate products inside
         * the same upload.
         *
         * Key:
         * master_product_name + category_name
         */
        Set<String> fileKeys = new HashSet<>();

        /*
         * Existing database products.
         */
        List<FmMasterProduct> existingProducts = masterProductRepository.findAllByOrderByMasterProductIdAsc();

        Set<String> existingKeys = new HashSet<>();

        for (FmMasterProduct existing : existingProducts) {

            if (isBlank(existing.getMasterProductName()) || isBlank(existing.getCategoryName())) {
                continue;
            }

            String key = buildDuplicateKey(existing.getMasterProductName(), existing.getCategoryName());

            existingKeys.add(key);
        }


        // ========================================================
        // PROCESS EACH EXCEL ROW
        // ========================================================

        for (int rowNumber = 0; rowNumber < requests.size(); rowNumber++) {

            FmMasterProductRequest req = requests.get(rowNumber);

            int excelRow = rowNumber + 2;

            if (req == null) {

                throw new BadRequestException("Product request cannot be null at Excel row " + excelRow);
            }

            validateBulkRequest(req, excelRow);

            String productName = req.getMasterProductName().trim();

            String categoryName = req.getCategoryName().trim();

            String duplicateKey = buildDuplicateKey(productName, categoryName);


            // ====================================================
            // DUPLICATE INSIDE FILE
            // ====================================================

            if (!fileKeys.add(duplicateKey)) {

                log.warn("[MASTER] Duplicate row inside upload skipped: " + "product={} category={} row={}", productName, categoryName, excelRow);

                continue;
            }


            // ====================================================
            // DUPLICATE IN DATABASE
            // ====================================================

            if (existingKeys.contains(duplicateKey)) {

                log.info("[MASTER] Existing product skipped: " + "product={} category={}", productName, categoryName);

                continue;
            }


            // ====================================================
            // CATEGORY
            // ====================================================

            Integer categoryId = resolveCategoryId(categoryName);

            if (categoryId == null || categoryId <= 0) {

                throw new BadRequestException("Unable to resolve category '" + categoryName + "' at Excel row " + excelRow);
            }


            // ====================================================
            // ENTITY
            // ====================================================

            FmMasterProduct entity = new FmMasterProduct();

            mapRequestToEntity(req, entity);

            /*
             * Use category resolved from
             * category_name.
             */
            entity.setCategoryId(categoryId);

            entity.setCategoryName(categoryName);


            // ====================================================
            // SYSTEM FIELDS
            // ====================================================

            entity.setCreatedAt(LocalDateTime.now());

            entity.setCreatedBy(req.getCreatedBy());

            entity.setUpdatedAt(null);

            entity.setUpdatedBy(null);

            entity.setIsActive("Y");


            // ====================================================
            // ADD TO INSERT LIST
            // ====================================================

            toInsert.add(entity);

            existingKeys.add(duplicateKey);

            log.info("[MASTER] Prepared bulk insert: " + "product={} category={} " + "categoryId={} isVeg={} " + "hasOptions={} productType={}", productName, categoryName, categoryId, entity.getIsVeg(), entity.getHasOptions(), entity.getProductType());
        }


        // ========================================================
        // NOTHING TO INSERT
        // ========================================================

        if (toInsert.isEmpty()) {

            log.info("[MASTER] Bulk insert completed: 0/{}", requests.size());

            return Collections.emptyList();
        }


        // ========================================================
        // SAVE
        // ========================================================

        List<FmMasterProduct> saved = masterProductRepository.saveAll(toInsert);

        log.info("[MASTER] Bulk insert completed: {}/{}", saved.size(), requests.size());

        return saved;
    }


    // ============================================================
    // MAP REQUEST -> ENTITY
    // ============================================================

    private void mapRequestToEntity(FmMasterProductRequest req, FmMasterProduct entity) {

        entity.setMasterProductName(trimToNull(req.getMasterProductName()));

        entity.setDescription(trimToNull(req.getDescription()));

        entity.setPhoto(trimToNull(req.getPhoto()));

        String categoryName = trimToNull(req.getCategoryName());

        entity.setCategoryName(categoryName);

        /*
         * For bulk imports category_id
         * can be omitted.
         */
        Integer categoryId = req.getCategoryId();

        if ((categoryId == null || categoryId <= 0) && !isBlank(categoryName)) {

            categoryId = resolveCategoryId(categoryName);
        }

        entity.setCategoryId(categoryId);


        // ========================================================
        // IS VEG
        // ========================================================

        Boolean isVeg = resolveIsVegFromRequest(req);

        if (isVeg == null) {

            throw new BadRequestException("is_veg is required for product: " + req.getMasterProductName());
        }

        entity.setIsVeg(isVeg);


        // ========================================================
        // CUISINE
        // ========================================================

        entity.setCuisineType(trimToNull(req.getCuisineType()));


        // ========================================================
        // HAS OPTIONS
        // ========================================================

        Integer hasOptions = req.getHasOptions();

        if (hasOptions == null) {

            throw new BadRequestException("has_options is required for product: " + req.getMasterProductName());
        }

        if (hasOptions != 0 && hasOptions != 1) {

            throw new BadRequestException("has_options must be 0 or 1 for product: " + req.getMasterProductName());
        }

        entity.setHasOptions(hasOptions);


        // ========================================================
        // OPTIONS
        // ========================================================

        String options = trimToNull(req.getOptions());

        if (hasOptions == 0) {

            entity.setOptions(null);

        } else {

            if (isBlank(options)) {

                throw new BadRequestException("options is required when " + "has_options = 1 for product: " + req.getMasterProductName());
            }

            entity.setOptions(options);
        }


        // ========================================================
        // PRODUCT TYPE
        // ========================================================

        String productType = trimToNull(req.getProductType());

        if (isBlank(productType)) {

            throw new BadRequestException("product_type is required for product: " + req.getMasterProductName());
        }

        if (productType.length() > 10) {

            throw new BadRequestException("product_type cannot exceed 10 characters for product: " + req.getMasterProductName());
        }

        entity.setProductType(productType);
    }


    // ============================================================
    // RESOLVE IS VEG
    // ============================================================

    private Boolean resolveIsVegFromRequest(FmMasterProductRequest req) {

        if (req == null) {
            return null;
        }

        return req.getIsVeg();
    }


    // ============================================================
    // VALIDATE REQUEST
    // ============================================================

    private void validateRequest(FmMasterProductRequest req) {

        if (isBlank(req.getMasterProductName())) {

            throw new BadRequestException("Master product name is required.");
        }

        if (req.getMasterProductName().trim().length() > 100) {

            throw new BadRequestException("Master product name cannot exceed 100 characters.");
        }


        if (isBlank(req.getCategoryName())) {

            throw new BadRequestException("Category name is required.");
        }

        if (req.getCategoryName().trim().length() > 100) {

            throw new BadRequestException("Category name cannot exceed 100 characters.");
        }


        /*
         * Normal create API still requires
         * category ID.
         *
         * Bulk import handles missing category_id
         * separately.
         */
        if (req.getCategoryId() == null || req.getCategoryId() <= 0) {

            throw new BadRequestException("Category ID is required.");
        }


        // ========================================================
        // IS VEG
        // ========================================================

        Boolean isVeg = resolveIsVegFromRequest(req);

        if (isVeg == null) {

            throw new BadRequestException("is_veg is required.");
        }


        // ========================================================
        // HAS OPTIONS
        // ========================================================

        if (req.getHasOptions() == null) {

            throw new BadRequestException("has_options is required.");
        }

        if (req.getHasOptions() != 0 && req.getHasOptions() != 1) {

            throw new BadRequestException("has_options must be 0 or 1.");
        }


        // ========================================================
        // OPTIONS
        // ========================================================

        if (req.getHasOptions() == 1 && isBlank(req.getOptions())) {

            throw new BadRequestException("options is required when has_options is 1.");
        }


        // ========================================================
        // PRODUCT TYPE
        // ========================================================

        if (isBlank(req.getProductType())) {

            throw new BadRequestException("Product type is required.");
        }

        if (req.getProductType().trim().length() > 10) {

            throw new BadRequestException("Product type cannot exceed 10 characters.");
        }
    }


    // ============================================================
    // VALIDATE BULK REQUEST
    // ============================================================

    private void validateBulkRequest(FmMasterProductRequest req, int rowNumber) {

        // --------------------------------------------------------
        // master_product_name
        // --------------------------------------------------------

        if (isBlank(req.getMasterProductName())) {

            throw new BadRequestException("master_product_name is required at Excel row " + rowNumber);
        }

        if (req.getMasterProductName().trim().length() > 100) {

            throw new BadRequestException("master_product_name exceeds 100 characters " + "at Excel row " + rowNumber);
        }


        // --------------------------------------------------------
        // category_id
        // --------------------------------------------------------

        /*
         * category_id intentionally NOT mandatory.
         *
         * category_name is used to resolve the
         * final category ID.
         */


        // --------------------------------------------------------
        // category_name
        // --------------------------------------------------------

        if (isBlank(req.getCategoryName())) {

            throw new BadRequestException("category_name is required at Excel row " + rowNumber);
        }

        if (req.getCategoryName().trim().length() > 100) {

            throw new BadRequestException("category_name exceeds 100 characters " + "at Excel row " + rowNumber);
        }


        // --------------------------------------------------------
        // is_veg
        // --------------------------------------------------------

        if (req.getIsVeg() == null) {

            throw new BadRequestException("is_veg must be true or false at Excel row " + rowNumber + ". The add-new-items request must contain isVeg=true or isVeg=false.");
        }


        // --------------------------------------------------------
        // has_options
        // --------------------------------------------------------

        if (req.getHasOptions() == null) {

            throw new BadRequestException("has_options is required at Excel row " + rowNumber);
        }

        if (req.getHasOptions() != 0 && req.getHasOptions() != 1) {

            throw new BadRequestException("has_options must be 0 or 1 at Excel row " + rowNumber);
        }


        // --------------------------------------------------------
        // options
        // --------------------------------------------------------

        if (req.getHasOptions() == 1 && isBlank(req.getOptions())) {

            throw new BadRequestException("options is required when has_options = 1 " + "at Excel row " + rowNumber);
        }


        // --------------------------------------------------------
        // product_type
        // --------------------------------------------------------

        if (isBlank(req.getProductType())) {

            throw new BadRequestException("product_type is required at Excel row " + rowNumber);
        }

        if (req.getProductType().trim().length() > 10) {

            throw new BadRequestException("product_type cannot exceed 10 characters " + "at Excel row " + rowNumber);
        }
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

        if (id == null) {

            throw new IllegalArgumentException("Master product ID cannot be null.");
        }

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

        if (id == null) {

            throw new IllegalArgumentException("Master product ID cannot be null.");
        }

        if (req == null) {

            throw new BadRequestException("Master product request cannot be null.");
        }


        FmMasterProduct existing = masterProductRepository.findById(id).orElseThrow(() -> new MasterProductNotFoundException(id));


        validateRequest(req);


        // ========================================================
        // DUPLICATE CHECK
        // ========================================================

        String newProductName = req.getMasterProductName().trim();

        String newCategoryName = req.getCategoryName().trim();

        boolean categoryChanged = !Objects.equals(existing.getCategoryId(), req.getCategoryId()) || !norm(existing.getCategoryName()).equals(norm(newCategoryName));

        boolean nameChanged = !norm(existing.getMasterProductName()).equals(norm(newProductName));


        if (nameChanged || categoryChanged) {

            boolean duplicate = masterProductRepository.existsByMasterProductNameIgnoreCaseAndCategoryNameIgnoreCase(newProductName, newCategoryName);

            if (duplicate) {

                FmMasterProduct duplicateProduct = findByNameAndCategory(newProductName, newCategoryName);

                if (duplicateProduct != null && !Objects.equals(duplicateProduct.getMasterProductId(), id)) {

                    throw new DuplicateResourceException("Master Product already exists in this category.");
                }
            }
        }


        // ========================================================
        // CATEGORY
        // ========================================================

        Integer categoryId = req.getCategoryId();

        if (categoryId == null || categoryId <= 0) {

            categoryId = resolveCategoryId(newCategoryName);

            if (categoryId == null) {

                throw new ResourceNotFoundException("Category not found: " + newCategoryName);
            }

            req.setCategoryId(categoryId);
        }


        // ========================================================
        // UPDATE ENTITY
        // ========================================================

        mapRequestToEntity(req, existing);

        existing.setCategoryId(categoryId);

        existing.setCategoryName(newCategoryName);


        // ========================================================
        // AUDIT
        // ========================================================

        existing.setUpdatedAt(LocalDateTime.now());

        if (req.getUpdatedBy() != null) {

            existing.setUpdatedBy(req.getUpdatedBy());
        }


        if (isBlank(existing.getIsActive())) {

            existing.setIsActive("Y");
        }


        FmMasterProduct saved = masterProductRepository.save(existing);


        log.info("[MASTER] Successfully updated product " + "id={} categoryId={} isVeg={} " + "productType={}", saved.getMasterProductId(), saved.getCategoryId(), saved.getIsVeg(), saved.getProductType());

        return saved;
    }


    // ============================================================
    // DELETE
    // ============================================================

    public void delete(Integer id) {

        if (id == null) {

            throw new IllegalArgumentException("Master product ID cannot be null.");
        }

        if (!masterProductRepository.existsById(id)) {

            throw new MasterProductNotFoundException(id);
        }

        masterProductRepository.deleteById(id);

        log.info("[MASTER] Deleted id={}", id);
    }


    // ============================================================
    // PHOTO UPLOAD
    // ============================================================

    public String updatePhoto(Integer id, MultipartFile photo) {

        if (id == null) {

            throw new IllegalArgumentException("Master product ID cannot be null.");
        }

        if (photo == null || photo.isEmpty()) {

            throw new IllegalArgumentException("Photo file cannot be empty.");
        }


        FmMasterProductMapper.validatePhoto(photo.getContentType(), photo.getSize());


        try {

            byte[] bytes = photo.getBytes();

            String base64 = Base64.getEncoder().encodeToString(bytes);

            String uri = "data:" + photo.getContentType() + ";base64," + base64;


            FmMasterProduct mp = masterProductRepository.findById(id).orElseThrow(() -> new MasterProductNotFoundException(id));


            mp.setPhoto(uri);

            mp.setUpdatedAt(LocalDateTime.now());

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
    // COMPARE FILE WITH DATABASE
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

            if (isBlank(d.getMasterProductName()) || isBlank(d.getCategoryName())) {

                continue;
            }

            String key = buildDuplicateKey(d.getMasterProductName(), d.getCategoryName());

            dbLookup.put(key, d);
        }


        List<FmCompareFileResponse.CompareItem> duplicates = new ArrayList<>();

        List<FmCompareFileResponse.CompareItem> newItems = new ArrayList<>();


        int skipped = 0;


        for (FmMasterProduct fp : parsed) {

            if (isBlank(fp.getMasterProductName())) {

                skipped++;

                log.warn("[MASTER] Skipping row: " + "missing master product name.");

                continue;
            }


            if (isBlank(fp.getCategoryName())) {

                skipped++;

                log.warn("[MASTER] Skipping product={} " + "because category name is missing.", fp.getMasterProductName());

                continue;
            }


            if (fp.getIsVeg() == null) {

                skipped++;

                log.warn("[MASTER] Skipping product={} " + "because is_veg is missing.", fp.getMasterProductName());

                continue;
            }


            // ====================================================
            // CATEGORY RESOLUTION
            // ====================================================

            Integer resolvedCategoryId = resolveCategoryId(fp.getCategoryName());

            if (resolvedCategoryId == null || resolvedCategoryId <= 0) {

                skipped++;

                log.warn("[MASTER] Skipping product={} " + "because category={} " + "could not be resolved.", fp.getMasterProductName(), fp.getCategoryName());

                continue;
            }

            fp.setCategoryId(resolvedCategoryId);


            String key = buildDuplicateKey(fp.getMasterProductName(), fp.getCategoryName());


            FmMasterProduct dbMatch = dbLookup.get(key);


            // ====================================================
            // DUPLICATE
            // ====================================================

            if (dbMatch != null) {

                /*
                 * IMPORTANT:
                 *
                 * Master product DB data comes from dbMatch.
                 *
                 * Merchant price, timing and day-of-week
                 * MUST come from the uploaded CSV row fp.
                 */
                duplicates.add(toCompareItem(dbMatch.getMasterProductId(), dbMatch, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));

            } else {

                /*
                 * New product values come from fp.
                 *
                 * CSV merchant price/timing/day are also
                 * preserved here.
                 */
                newItems.add(toCompareItem(null, fp, fp.getCsvMerchantPrice(), fp.getCsvTiming(), fp.getCsvDayOfWeek()));
            }
        }


        log.info("[MASTER] Compare: dup={} new={} skipped={}", duplicates.size(), newItems.size(), skipped);


        return new FmCompareFileResponse(duplicates, newItems, parsed.size(), duplicates.size(), newItems.size(), skipped);
    }


    // ============================================================
    // CREATE COMPARE ITEM
    // ============================================================

    private FmCompareFileResponse.CompareItem toCompareItem(Integer id, FmMasterProduct mp, Double merchantPrice, String csvTiming, String csvDayOfWeek) {

        if (mp == null) {

            throw new IllegalArgumentException("Master product cannot be null while creating compare item.");
        }


        log.info("[MASTER] Creating compare item: " + "product='{}', id={}, isVeg={}, " + "categoryId={}, categoryName={}, " + "hasOptions={}, productType={}, " + "merchantPrice={}, csvTiming={}, " + "csvDayOfWeek={}", mp.getMasterProductName(), id, mp.getIsVeg(), mp.getCategoryId(), mp.getCategoryName(), mp.getHasOptions(), mp.getProductType(), merchantPrice, csvTiming, csvDayOfWeek);


        return new FmCompareFileResponse.CompareItem(id,

                // DB / Master Product fields
                mp.getMasterProductName(), mp.getDescription(), mp.getPhoto(), mp.getCategoryId(), mp.getCategoryName(), mp.getIsVeg(), mp.getCuisineType(), mp.getHasOptions(), mp.getOptions(), mp.getProductType(),

                // CSV / Cache fields
                merchantPrice, csvTiming, csvDayOfWeek);
    }


    // ============================================================
    // CSV / EXCEL PARSER
    // ============================================================

    /**
     * FileConverterService converts Excel to CSV.
     * <p>
     * Supported master-product fields:
     * <p>
     * master_product_name
     * description
     * photo
     * category_id
     * category_name
     * is_veg
     * cuisine_type
     * has_options
     * options
     * product_type
     * <p>
     * Optional CSV/cache fields:
     * <p>
     * merchant_price
     * timing
     * daysofaweek
     * <p>
     * IMPORTANT:
     * <p>
     * merchant_price, timing and daysofaweek are
     * NOT stored in master_products.
     * <p>
     * They are retained temporarily so the
     * Add To Outlet Products screen can use
     * the uploaded values automatically.
     */
    private List<FmMasterProduct> parseCsv(InputStream stream) {

        List<FmMasterProduct> list = new ArrayList<>();


        /*
         * Upload may be:
         *
         * comma-separated
         * tab-separated
         * semicolon-separated
         */
        try {

            byte[] csvBytes = stream.readAllBytes();

            if (csvBytes.length == 0) {

                throw new IllegalArgumentException("Uploaded CSV file is empty.");
            }


            String firstLine = new String(csvBytes, StandardCharsets.UTF_8);

            int lineEnd = firstLine.indexOf('\n');

            if (lineEnd >= 0) {

                firstLine = firstLine.substring(0, lineEnd);
            }


            firstLine = firstLine.replace("\uFEFF", "");


            char separator;


            if (firstLine.indexOf('\t') >= 0) {

                separator = '\t';

                log.info("[MASTER] Detected TAB-separated CSV/Excel file.");

            } else if (firstLine.indexOf(';') >= 0 && firstLine.indexOf(',') < 0) {

                separator = ';';

                log.info("[MASTER] Detected SEMICOLON-separated CSV file.");

            } else {

                separator = ',';

                log.info("[MASTER] Detected COMMA-separated CSV file.");
            }


            try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8)).withCSVParser(new CSVParserBuilder().withSeparator(separator).build()).build()) {

                String[] row;

                boolean header = true;


                // ====================================================
                // COLUMN INDEXES
                // ====================================================

                int nameIdx = -1;

                int descriptionIdx = -1;

                int photoIdx = -1;

                int categoryIdIdx = -1;

                int categoryNameIdx = -1;

                int isVegIdx = -1;

                int cuisineTypeIdx = -1;

                int hasOptionsIdx = -1;

                int optionsIdx = -1;

                int productTypeIdx = -1;

                /*
                 * CSV-only fields.
                 */
                int merchantPriceIdx = -1;

                int timingIdx = -1;

                int dayOfWeekIdx = -1;


                // ====================================================
                // READ ROWS
                // ====================================================

                while ((row = reader.readNext()) != null) {

                    // =================================================
                    // HEADER
                    // =================================================

                    if (header) {

                        header = false;

                        log.info("[MASTER] CSV headers detected: {}", Arrays.toString(row));


                        for (int i = 0; i < row.length; i++) {

                            String h = normalizeHeader(row[i]);


                            // -----------------------------------------
                            // NAME
                            // -----------------------------------------

                            if ("master_product_name".equals(h) || "masterproductname".equals(h) || "name".equals(h)) {

                                nameIdx = i;
                            }


                            // -----------------------------------------
                            // DESCRIPTION
                            // -----------------------------------------

                            else if ("description".equals(h)) {

                                descriptionIdx = i;
                            }


                            // -----------------------------------------
                            // PHOTO
                            // -----------------------------------------

                            else if ("photo".equals(h)) {

                                photoIdx = i;
                            }


                            // -----------------------------------------
                            // CATEGORY ID
                            // -----------------------------------------

                            else if ("category_id".equals(h) || "categoryid".equals(h)) {

                                categoryIdIdx = i;
                            }


                            // -----------------------------------------
                            // CATEGORY NAME
                            // -----------------------------------------

                            else if ("category_name".equals(h) || "categoryname".equals(h) || "category".equals(h)) {

                                categoryNameIdx = i;
                            }


                            // -----------------------------------------
                            // IS VEG
                            // -----------------------------------------

                            else if ("is_veg".equals(h) || "isveg".equals(h)) {

                                isVegIdx = i;
                            }


                            // -----------------------------------------
                            // CUISINE
                            // -----------------------------------------

                            else if ("cuisine_type".equals(h) || "cuisinetype".equals(h)) {

                                cuisineTypeIdx = i;
                            }


                            // -----------------------------------------
                            // HAS OPTIONS
                            // -----------------------------------------

                            else if ("has_options".equals(h) || "hasoptions".equals(h)) {

                                hasOptionsIdx = i;
                            }


                            // -----------------------------------------
                            // OPTIONS
                            // -----------------------------------------

                            else if ("options".equals(h)) {

                                optionsIdx = i;
                            }


                            // -----------------------------------------
                            // PRODUCT TYPE
                            // -----------------------------------------

                            else if ("product_type".equals(h) || "producttype".equals(h)) {

                                productTypeIdx = i;
                            }


                            // -----------------------------------------
                            // MERCHANT PRICE
                            // -----------------------------------------

                            else if ("merchant_price".equals(h) || "merchantprice".equals(h) || "price".equals(h)) {

                                merchantPriceIdx = i;
                            }


                            // -----------------------------------------
                            // TIMING
                            // -----------------------------------------

                            else if ("timing".equals(h) || "time".equals(h)) {

                                timingIdx = i;
                            }


                            // -----------------------------------------
                            // DAY OF WEEK
                            // -----------------------------------------

                            else if ("daysofaweek".equals(h) || "day_of_week".equals(h) || "dayofweek".equals(h) || "days_of_a_week".equals(h)) {

                                dayOfWeekIdx = i;
                            }
                        }


                        // =================================================
                        // REQUIRED HEADER VALIDATION
                        // =================================================

                        if (nameIdx < 0) {

                            throw new IllegalArgumentException("Required CSV column " + "'master_product_name' " + "was not found.");
                        }


                        /*
                         * category_id is optional.
                         */
                        if (categoryIdIdx < 0) {

                            log.info("[MASTER] CSV has no category_id column. " + "Category ID will be resolved from category_name.");
                        }


                        if (categoryNameIdx < 0) {

                            throw new IllegalArgumentException("Required CSV column " + "'category_name' " + "was not found.");
                        }


                        if (isVegIdx < 0) {

                            throw new IllegalArgumentException("Required CSV column " + "'is_veg' " + "was not found. " + "Use true or false.");
                        }


                        if (hasOptionsIdx < 0) {

                            throw new IllegalArgumentException("Required CSV column " + "'has_options' " + "was not found.");
                        }


                        if (productTypeIdx < 0) {

                            throw new IllegalArgumentException("Required CSV column " + "'product_type' " + "was not found.");
                        }


                        log.info("[MASTER] Header mapping completed: " + "name={} description={} photo={} " + "categoryId={} categoryName={} " + "isVeg={} cuisine={} " + "hasOptions={} options={} " + "productType={} " + "merchantPrice={} timing={} " + "dayOfWeek={}", nameIdx, descriptionIdx, photoIdx, categoryIdIdx, categoryNameIdx, isVegIdx, cuisineTypeIdx, hasOptionsIdx, optionsIdx, productTypeIdx, merchantPriceIdx, timingIdx, dayOfWeekIdx);


                        continue;
                    }


                    // =================================================
                    // IGNORE EMPTY ROW
                    // =================================================

                    if (isEmptyRow(row)) {

                        continue;
                    }


                    // =================================================
                    // CREATE ENTITY
                    // =================================================

                    FmMasterProduct mp = new FmMasterProduct();


                    // =================================================
                    // NAME
                    // =================================================

                    String productName = safeGet(row, nameIdx);

                    mp.setMasterProductName(trimToNull(productName));


                    // =================================================
                    // DESCRIPTION
                    // =================================================

                    mp.setDescription(trimToNull(safeGet(row, descriptionIdx)));


                    // =================================================
                    // PHOTO
                    // =================================================

                    mp.setPhoto(trimToNull(safeGet(row, photoIdx)));


                    // =================================================
                    // CATEGORY NAME
                    // =================================================

                    String categoryName = trimToNull(safeGet(row, categoryNameIdx));

                    mp.setCategoryName(categoryName);


                    // =================================================
                    // CATEGORY ID - OPTIONAL
                    // =================================================

                    String categoryIdValue = safeGet(row, categoryIdIdx);

                    if (!isBlank(categoryIdValue)) {

                        Integer suppliedCategoryId = parseInteger(categoryIdValue, "category_id", productName);

                        if (suppliedCategoryId != null && suppliedCategoryId > 0) {

                            mp.setCategoryId(suppliedCategoryId);
                        }
                    }


                    // =================================================
                    // IS VEG
                    // =================================================

                    Boolean isVeg = parseIsVeg(row, isVegIdx, productName);

                    if (isVeg == null) {

                        throw new IllegalArgumentException("is_veg must be true or false " + "for product: " + productName);
                    }

                    mp.setIsVeg(isVeg);


                    // =================================================
                    // CUISINE
                    // =================================================

                    mp.setCuisineType(trimToNull(safeGet(row, cuisineTypeIdx)));


                    // =================================================
                    // HAS OPTIONS
                    // =================================================

                    String hasOptionsValue = safeGet(row, hasOptionsIdx);

                    if (isBlank(hasOptionsValue)) {

                        throw new IllegalArgumentException("has_options is required for product: " + productName);
                    }

                    Integer hasOptions = parseBinaryInteger(hasOptionsValue, "has_options", productName);

                    mp.setHasOptions(hasOptions);


                    // =================================================
                    // OPTIONS
                    // =================================================

                    String options = trimToNull(safeGetRaw(row, optionsIdx));

                    if (hasOptions == 0) {

                        mp.setOptions(null);

                    } else {

                        if (isBlank(options)) {

                            throw new IllegalArgumentException("options is required when " + "has_options = 1 " + "for product: " + productName);
                        }

                        mp.setOptions(options);
                    }


                    // =================================================
                    // PRODUCT TYPE
                    // =================================================

                    String productType = trimToNull(safeGet(row, productTypeIdx));


                    if (isBlank(productType)) {

                        throw new IllegalArgumentException("product_type is required for product: " + productName);
                    }


                    if (productType.length() > 10) {

                        throw new IllegalArgumentException("product_type exceeds 10 characters " + "for product: " + productName);
                    }


                    mp.setProductType(productType);


                    // =================================================
                    // MERCHANT PRICE FROM CSV / EXCEL
                    // =================================================

                    String merchantPriceValue = safeGet(row, merchantPriceIdx);

                    if (!isBlank(merchantPriceValue)) {

                        try {

                            Double merchantPrice = Double.parseDouble(merchantPriceValue.trim());

                            mp.setCsvMerchantPrice(merchantPrice);

                        } catch (NumberFormatException e) {

                            throw new IllegalArgumentException("Invalid merchant_price value '" + merchantPriceValue + "' for product: " + productName);
                        }
                    }


                    // =================================================
                    // TIMING FROM CSV / EXCEL
                    // =================================================

                    String timingValue = safeGet(row, timingIdx);

                    mp.setCsvTiming(trimToNull(timingValue));


                    // =================================================
                    // DAY OF WEEK FROM CSV / EXCEL
                    // =================================================

                    String dayOfWeekValue = safeGet(row, dayOfWeekIdx);

                    mp.setCsvDayOfWeek(trimToNull(dayOfWeekValue));


                    // =================================================
                    // VALIDATE REQUIRED VALUES
                    // =================================================

                    if (isBlank(mp.getMasterProductName())) {

                        throw new IllegalArgumentException("master_product_name is required.");
                    }


                    if (isBlank(mp.getCategoryName())) {

                        throw new IllegalArgumentException("category_name is required for product: " + mp.getMasterProductName());
                    }


                    if (mp.getIsVeg() == null) {

                        throw new IllegalArgumentException("is_veg must be true or false " + "for product: " + mp.getMasterProductName());
                    }


                    log.debug("[MASTER] Parsed CSV product: " + "name={}, merchantPrice={}, " + "timing={}, dayOfWeek={}", mp.getMasterProductName(), mp.getCsvMerchantPrice(), mp.getCsvTiming(), mp.getCsvDayOfWeek());


                    // =================================================
                    // ADD
                    // =================================================

                    list.add(mp);
                }
            }

        } catch (FileProcessingException e) {

            throw e;

        } catch (Exception e) {

            log.error("[MASTER] File parse failed", e);

            throw new FileProcessingException("CSV/Excel parse error: " + e.getMessage(), e);
        }


        log.info("[MASTER] CSV parsing completed. Total rows={}", list.size());


        return list;
    }


    // ============================================================
    // PARSE IS VEG
    // ============================================================

    private Boolean parseIsVeg(String[] row, int isVegIdx, String productName) {

        if (isVegIdx < 0) {

            return null;
        }

        String value = safeGet(row, isVegIdx);

        if (isBlank(value)) {

            return null;
        }

        return parseBoolean(value, "is_veg", productName);
    }


    // ============================================================
    // PARSE BOOLEAN
    // ============================================================

    private Boolean parseBoolean(String value, String fieldName, String productName) {

        if (isBlank(value)) {

            return null;
        }

        String normalized = norm(value);


        if ("true".equals(normalized)) {

            return true;
        }


        if ("false".equals(normalized)) {

            return false;
        }


        /*
         * Common CSV typo:
         *
         * flase -> false
         */
        if ("flase".equals(normalized)) {

            log.warn("[MASTER] Corrected typo {}='{}' " + "to false for product={}", fieldName, value, productName);

            return false;
        }


        throw new IllegalArgumentException("Invalid " + fieldName + " value '" + value + "'. Expected true or false for product: " + productName);
    }


    // ============================================================
    // PARSE INTEGER
    // ============================================================

    private Integer parseInteger(String value, String fieldName, String productName) {

        if (isBlank(value)) {

            return null;
        }


        try {

            double number = Double.parseDouble(value.trim());

            if (number != Math.floor(number)) {

                throw new NumberFormatException("Decimal value is not allowed");
            }

            return (int) number;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Invalid " + fieldName + " value '" + value + "' for product: " + productName);
        }
    }


    // ============================================================
    // PARSE 0 / 1
    // ============================================================

    private Integer parseBinaryInteger(String value, String fieldName, String productName) {

        Integer result = parseInteger(value, fieldName, productName);

        if (result == null) {

            throw new IllegalArgumentException(fieldName + " is required for product: " + productName);
        }


        if (result != 0 && result != 1) {

            throw new IllegalArgumentException(fieldName + " must be 0 or 1 for product: " + productName);
        }


        return result;
    }


    // ============================================================
    // CATEGORY RESOLUTION
    // ============================================================

    private Integer resolveCategoryId(String categoryName) {

        if (isBlank(categoryName)) {

            return null;
        }


        String normalizedCategoryName = categoryName.trim();


        FmCategory category = categoryRepository.findByCategoryNameIgnoreCase(normalizedCategoryName).orElseGet(() -> {

            log.info("[CATEGORY] Category not found. " + "Creating new category: {}", normalizedCategoryName);


            FmCategory newCategory = new FmCategory();

            newCategory.setCategoryName(normalizedCategoryName);

            /*
             * fm_category.category_type
             * is NOT NULL.
             */
            newCategory.setCategoryType("HOME");


            FmCategory savedCategory = categoryRepository.save(newCategory);


            log.info("[CATEGORY] Created category id={} name={}", savedCategory.getCategoryId(), savedCategory.getCategoryName());


            return savedCategory;
        });


        log.info("[CATEGORY] Resolved category id={} name={}", category.getCategoryId(), category.getCategoryName());


        return category.getCategoryId();
    }


    // ============================================================
    // FIND EXISTING PRODUCT
    // ============================================================

    private FmMasterProduct findByNameAndCategory(String productName, String categoryName) {

        List<FmMasterProduct> all = masterProductRepository.findAllByOrderByMasterProductIdAsc();


        String expected = buildDuplicateKey(productName, categoryName);


        for (FmMasterProduct product : all) {

            String actual = buildDuplicateKey(product.getMasterProductName(), product.getCategoryName());


            if (expected.equals(actual)) {

                return product;
            }
        }


        return null;
    }


    // ============================================================
    // DUPLICATE KEY
    // ============================================================

    private String buildDuplicateKey(String productName, String categoryName) {

        return norm(productName) + "|" + norm(categoryName);
    }


    // ============================================================
    // GET PRODUCTS BY CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public List<FmMasterProductResponseDto> getProductsByCategory(Integer categoryId, String keyword) {

        log.info("GET_PRODUCTS_BY_CATEGORY_STARTED | " + "categoryId={} | keyword={}", categoryId, keyword);


        if (categoryId == null) {

            throw new IllegalArgumentException("Category ID cannot be null.");
        }


        if (!categoryRepository.existsById(categoryId)) {

            log.warn("CATEGORY_NOT_FOUND | categoryId={}", categoryId);

            throw new ResourceNotFoundException("Category not found with id : " + categoryId);
        }


        List<FmMasterProduct> products = masterProductRepository.findProductsByCategoryAndKeyword(categoryId, keyword);


        log.info("GET_PRODUCTS_BY_CATEGORY_COMPLETED | " + "categoryId={} | productCount={}", categoryId, products.size());


        return products.stream().map(masterProductMapper::toResponseDto).toList();
    }


    // ============================================================
    // CREATE MASTER PRODUCT
    // ============================================================

    @Transactional
    public FmCreateMasterProductResponseDto createMasterProduct(FmCreateMasterProductRequestDto request) {

        if (request == null) {

            throw new BadRequestException("Create master product request cannot be null.");
        }


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


        // ========================================================
        // CATEGORY
        // ========================================================

        FmCategory category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found with id : " + request.getCategoryId()));


        // ========================================================
        // FOOD TYPE VALIDATION
        // ========================================================

        if (request.getFoodType() != null) {

            if (Boolean.TRUE.equals(request.getIsVeg()) && !"VEG".equalsIgnoreCase(request.getFoodType())) {

                throw new BadRequestException("Veg products must have food type as VEG.");
            }


            if (Boolean.FALSE.equals(request.getIsVeg()) && "VEG".equalsIgnoreCase(request.getFoodType())) {

                throw new BadRequestException("Non Veg products cannot have food type as VEG.");
            }
        }


        // ========================================================
        // DUPLICATE
        // ========================================================

        if (masterProductRepository.existsByMasterProductNameIgnoreCaseAndCategoryId(request.getMasterProductName(), request.getCategoryId())) {

            throw new DuplicateResourceException("Master Product already exists in this category.");
        }


        // ========================================================
        // CREATE ENTITY
        // ========================================================

        FmMasterProduct entity = FmCreateMasterProductMapper.toEntity(request, category.getCategoryName(), 1);


        // ========================================================
        // SYSTEM FIELDS
        // ========================================================

        if (entity.getCreatedAt() == null) {

            entity.setCreatedAt(LocalDateTime.now());
        }


        if (entity.getCreatedBy() == null) {

            entity.setCreatedBy(1);
        }


        if (isBlank(entity.getIsActive())) {

            entity.setIsActive("Y");
        }


        FmMasterProduct savedProduct = masterProductRepository.save(entity);


        log.info("CREATE_MASTER_PRODUCT_COMPLETED | " + "masterProductId={} | " + "isVeg={} | productType={}", savedProduct.getMasterProductId(), savedProduct.getIsVeg(), savedProduct.getProductType());


        return mapper.toResponseDto(savedProduct);
    }


    // ============================================================
    // SAFE GET
    // ============================================================

    private String safeGet(String[] row, int idx) {

        if (row == null || idx < 0 || idx >= row.length) {

            return null;
        }


        if (row[idx] == null) {

            return null;
        }


        return row[idx].replace("\uFEFF", "").replace("\r", "").replace("\n", "").trim();
    }


    // ============================================================
    // SAFE RAW GET
    // ============================================================

    private String safeGetRaw(String[] row, int idx) {

        if (row == null || idx < 0 || idx >= row.length) {

            return null;
        }


        String value = row[idx];


        if (value == null) {

            return null;
        }


        value = value.replace("\uFEFF", "").replace("\r", "").trim();


        return value.isEmpty() ? null : value;
    }


    // ============================================================
    // NORMALIZE
    // ============================================================

    private String norm(String value) {

        if (value == null) {

            return "";
        }


        return value.replace("\uFEFF", "").replace("\"", "").replace("\r", "").replace("\n", "").trim().toLowerCase().replaceAll("\\s+", " ");
    }


    // ============================================================
    // NORMALIZE HEADER
    // ============================================================

    private String normalizeHeader(String value) {

        if (value == null) {

            return "";
        }


        return value.replace("\uFEFF", "").replace("\"", "").replace("\r", "").replace("\n", "").trim().toLowerCase();
    }


    // ============================================================
    // TRIM TO NULL
    // ============================================================

    private String trimToNull(String value) {

        if (value == null) {

            return null;
        }


        String trimmed = value.trim();


        return trimmed.isEmpty() ? null : trimmed;
    }


    // ============================================================
    // BLANK CHECK
    // ============================================================

    private boolean isBlank(String value) {

        return value == null || value.trim().isEmpty();
    }


    // ============================================================
    // EMPTY CSV ROW
    // ============================================================

    private boolean isEmptyRow(String[] row) {

        if (row == null || row.length == 0) {

            return true;
        }


        for (String value : row) {

            if (!isBlank(value)) {

                return false;
            }
        }


        return true;
    }
}