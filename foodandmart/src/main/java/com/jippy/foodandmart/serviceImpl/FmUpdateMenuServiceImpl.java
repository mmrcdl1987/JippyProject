package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmOutletCategoryDTO;
import com.jippy.foodandmart.dto.FmProductDto;
import com.jippy.foodandmart.dto.FmProductVariantDTO;
import com.jippy.foodandmart.dto.FmUpdateMenuResultDTO;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.IFmUpdateMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for reading and uploading outlet menus.
 *
 * <p>Why separate from {@link FmMenuServiceImpl}: MenuService handles menu-copy
 * operations (outlet-to-outlet cloning). UpdateMenuService handles the
 * CSV/spreadsheet upload flow where an outlet manager uploads a structured
 * file to define their product catalogue with categories, prices and variants.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmUpdateMenuServiceImpl implements IFmUpdateMenuService {

    private final FmOutletRepository outletRepository;
    private final FmCategoryRepository categoryRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmProductRepository productRepository;
    private final FmProductVariantRepository productVariantRepository;

    /**
     * Returns the full menu for an outlet, grouped by category, with products
     * and variants nested.
     *
     * <p>Why a nested structure (category → products → variants): the frontend
     * renders the menu in an accordion grouped by category. A flat product list
     * would require the frontend to do the grouping, which is less efficient.</p>
     *
     * @param outletId the outlet's primary key
     * @return list of {@link FmOutletCategoryDTO}, each containing its products and their variants
     * @throws IllegalArgumentException if the outlet does not exist
     */
    @Override
    public List<FmOutletCategoryDTO> getMenuByOutlet(Integer outletId) {
        if (!outletRepository.existsById(outletId))
            throw new IllegalArgumentException("Outlet ID " + outletId + " does not exist");

        List<FmOutletCategory> outletCats = outletCategoryRepository.findByOutletId(outletId);
        List<FmOutletCategoryDTO> result = new ArrayList<>();

        for (FmOutletCategory oc : outletCats) {
            FmCategory cat = categoryRepository.findById(oc.getCategoryId()).orElse(null);
            String catName = cat != null ? cat.getCategoryName() : "Unknown";

            // Build product DTOs with nested variant lists
            List<FmProductDto> productDTOs = productRepository.findByOutletCategoryId(oc.getOutletCategoryId())
                    .stream().map(p -> {
                        List<FmProductVariantDTO> variants = productVariantRepository.findByProductId(p.getProductId())
                                .stream()
                                .map(v -> {
                                    FmProductVariantDTO vDto = new FmProductVariantDTO();
                                    vDto.setVariantId(v.getProductVariantId());
                                    vDto.setVariantName(v.getVariantName());
                                    vDto.setMerchantPrice(v.getMerchantPrice());
                                    return vDto;
                                })
                                .collect(Collectors.toList());

                        FmProductDto pDto = new FmProductDto();
                        pDto.setProductId(p.getProductId());
                        pDto.setProductName(p.getProductName());
                        pDto.setDescription(p.getDescription());
                        pDto.setMerchantPrice(p.getMerchantPrice());
                        pDto.setIsVeg(p.getIsVeg());
                        pDto.setHasProductVariants(p.getHasProductVariants());
                        pDto.setVariants(variants);
                        return pDto;
                    }).collect(Collectors.toList());

            FmOutletCategoryDTO catDto = new FmOutletCategoryDTO();
            catDto.setOutletCategoryId(oc.getOutletCategoryId());
            catDto.setCategoryId(oc.getCategoryId());
            catDto.setCategoryName(catName);
            catDto.setProducts(productDTOs);
            result.add(catDto);
        }
        return result;
    }

    /**
     * Processes a parsed CSV/Excel file as a list of row maps and upserts
     * categories, products, and variants for the specified outlet.
     *
     * <p>Why {@code @Transactional} on the upload: the entire file should
     * succeed or fail together. If a mid-file error occurs the partial import
     * is rolled back and the outlet's menu remains unchanged.</p>
     *
     * <p>Row keys are normalised (lowercase, no underscores) so column names
     * like "ProductName", "product_name", and "productname" all match the same
     * key. See {@link #get(Map, String, String)} for the normalisation logic.</p>
     *
     * <p>Variants are encoded in one cell as {@code "Name:Price,Name:Price"}
     * (e.g. {@code "Small:99,Medium:149"}). When variants are present the
     * base product price is set to the minimum variant price so sorting by
     * price still works.</p>
     *
     * @param rows     parsed rows from the file (each row is a map of normalised column → value)
     * @param outletId the outlet whose menu this file belongs to
     * @return an {@link FmUpdateMenuResultDTO} with row-level error details
     * @throws IllegalArgumentException if the outlet does not exist
     */
    @Override
    @Transactional
    public FmUpdateMenuResultDTO uploadMenu(List<Map<String, String>> rows, Integer outletId) {
        FmOutlet outlet = outletRepository.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Outlet ID " + outletId + " does not exist"));

        int totalRows = rows.size(), categoriesCreated = 0, productsCreated = 0,
                productsUpdated = 0, variantsCreated = 0, failureCount = 0;
        List<FmUpdateMenuResultDTO.RowError> errors = new ArrayList<>();
        // Cache category IDs per outlet to avoid redundant DB lookups for each row
        Map<String, Integer> catCache = new HashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // 1-based; row 1 = header
            Map<String, String> row = rows.get(i);
            try {
                String categoryName = required(row, "category", rowNum);
                String productName  = required(row, "productname", rowNum);
                String description  = get(row, "description", "");
                // Accept "merchantprice" as an alias for "price" to support
                // CSV uploads that use the column name used in the DB / UI
                String rawPrice     = get(row, "price", null);
                if (rawPrice == null) rawPrice = get(row, "merchantprice", "0");
                BigDecimal price    = parseBD(rawPrice);
                boolean isVeg       = parseVeg(get(row, "isveg", "true"));
                String variantsStr  = get(row, "variants", "");

                String catKey = categoryName.trim().toLowerCase();
                Integer outletCategoryId = catCache.get(catKey);

                if (outletCategoryId == null) {
                    // Get or create the global category record
                    FmCategory cat = categoryRepository.findByCategoryNameIgnoreCase(categoryName.trim())
                            .orElseGet(() -> {
                                FmCategory newCat = new FmCategory();
                                // Capitalise first letter for consistent display in the UI
                                newCat.setCategoryName(capitalize(categoryName.trim()));
                                return categoryRepository.save(newCat);
                            });

                    // Check existence before orElseGet so we can track new vs existing outside lambda
                    boolean ocAlreadyExists = outletCategoryRepository
                            .findByOutletIdAndCategoryId(outletId, cat.getCategoryId())
                            .isPresent();

                    FmOutletCategory oc = outletCategoryRepository
                            .findByOutletIdAndCategoryId(outletId, cat.getCategoryId())
                            .orElseGet(() -> {
                                FmOutletCategory newOc = new FmOutletCategory();
                                newOc.setOutletId(outletId);
                                newOc.setCategoryId(cat.getCategoryId());
                                return outletCategoryRepository.save(newOc);
                            });

                    // Only count as "created" if it didn't already exist
                    if (!ocAlreadyExists) categoriesCreated++;

                    outletCategoryId = oc.getOutletCategoryId();
                    catCache.put(catKey, outletCategoryId);
                }

                List<String[]> variantPairs = parseVariants(variantsStr);
                boolean hasVariants = !variantPairs.isEmpty();
                if (hasVariants) {
                    // Set base product price to minimum variant price for sensible sorting
                    price = variantPairs.stream().map(vp -> parseBD(vp[1]))
                            .min(Comparator.naturalOrder()).orElse(price);
                }

                final int finalOutletCategoryId = outletCategoryId;
                Optional<FmProduct> existing = productRepository
                        .findByOutletCategoryIdAndProductNameIgnoreCase(finalOutletCategoryId, productName.trim());

                FmProduct product;
                if (existing.isPresent()) {
                    // Update fields on existing product rather than creating a duplicate
                    product = existing.get();
                    product.setDescription(description);
                    product.setMerchantPrice(price);
                    product.setIsVeg(isVeg);
                    product.setHasProductVariants(hasVariants);
                    product = productRepository.save(product);
                    productsUpdated++;
                } else {
                    product = new FmProduct();
                    product.setOutletCategoryId(finalOutletCategoryId);
                    product.setProductName(productName.trim());
                    product.setDescription(description);
                    product.setMerchantPrice(price);
                    product.setIsVeg(isVeg);
                    product.setHasProductVariants(hasVariants);
                    product = productRepository.save(product);
                    productsCreated++;
                }

                if (hasVariants) {
                    // Delete existing variants before re-inserting so re-uploads replace old data
                    productVariantRepository.deleteByProductId(product.getProductId());
                    for (String[] vp : variantPairs) {
                        FmProductVariant variant = new FmProductVariant();
                        variant.setProductId(product.getProductId());
                        variant.setVariantName(vp[0].trim());
                        variant.setMerchantPrice(parseBD(vp[1]));
                        productVariantRepository.save(variant);
                        variantsCreated++;
                    }
                }
            } catch (Exception e) {
                failureCount++;
                String pName = row.getOrDefault("productname", row.getOrDefault("name", "(unknown)"));
                log.warn("[MENU] Row {} failed: {}", rowNum, e.getMessage());
                FmUpdateMenuResultDTO.RowError err = new FmUpdateMenuResultDTO.RowError();
                err.setRowNumber(rowNum);
                err.setProductName(pName);
                err.setReason(e.getMessage());
                errors.add(err);
            }
        }

        log.info("[MENU] Upload complete for outletId={}: created={}, updated={}, failed={}",
                outletId, productsCreated, productsUpdated, failureCount);

        FmUpdateMenuResultDTO result = new FmUpdateMenuResultDTO();
        result.setOutletId(outletId);
        result.setOutletName(outlet.getOutletName());
        result.setTotalRows(totalRows);
        result.setCategoriesCreated(categoriesCreated);
        result.setProductsCreated(productsCreated);
        result.setProductsUpdated(productsUpdated);
        result.setVariantsCreated(variantsCreated);
        result.setFailureCount(failureCount);
        result.setErrors(errors);
        return result;
    }


    /**
     * Maps (price-updates only) existing outlet products from a CSV/Excel upload.
     *
     * <p>Matches each row by category name + product name against the outlet's
     * existing catalogue. Only {@code merchantPrice} is updated — no new products
     * or categories are created. Unmatched rows are recorded as errors so the
     * caller knows which rows had no match.</p>
     *
     * <p>Column aliases accepted for price: {@code price} or {@code merchantprice}.</p>
     *
     * @param rows     parsed rows from the uploaded file
     * @param outletId the outlet whose products should be price-updated
     * @return a result DTO with counts and per-row error details
     */
    @Override
    @Transactional
    public FmUpdateMenuResultDTO mapMenuFromCsv(List<Map<String, String>> rows, Integer outletId) {
        FmOutlet outlet = outletRepository.findById(outletId)
                .orElseThrow(() -> new IllegalArgumentException("Outlet ID " + outletId + " does not exist"));

        // Pre-load the outlet's category → outletCategoryId map for fast lookup
        Map<String, Integer> catNameToOcId = new HashMap<>();
        for (FmOutletCategory oc : outletCategoryRepository.findByOutletId(outletId)) {
            FmCategory cat = categoryRepository.findById(oc.getCategoryId()).orElse(null);
            if (cat != null) {
                catNameToOcId.put(cat.getCategoryName().trim().toLowerCase(), oc.getOutletCategoryId());
            }
        }

        int totalRows = rows.size(), productsUpdated = 0, failureCount = 0;
        List<FmUpdateMenuResultDTO.RowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);
            try {
                String categoryName = required(row, "category", rowNum);
                String productName  = required(row, "productname", rowNum);

                // Accept both "price" and "merchantprice" column names
                String rawPrice = get(row, "price", null);
                if (rawPrice == null) rawPrice = get(row, "merchantprice", "0");
                BigDecimal price = parseBD(rawPrice);

                String catKey = categoryName.trim().toLowerCase();
                Integer outletCategoryId = catNameToOcId.get(catKey);

                if (outletCategoryId == null) {
                    throw new IllegalArgumentException(
                            "Category '" + categoryName + "' not found in outlet " + outletId);
                }

                Optional<FmProduct> existing = productRepository
                        .findByOutletCategoryIdAndProductNameIgnoreCase(outletCategoryId, productName.trim());

                if (existing.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Product '" + productName + "' not found under category '" + categoryName + "'");
                }

                FmProduct product = existing.get();
                product.setMerchantPrice(price);
                productRepository.save(product);
                productsUpdated++;

            } catch (Exception e) {
                failureCount++;
                String pName = row.getOrDefault("productname", row.getOrDefault("name", "(unknown)"));
                log.warn("[MENU-MAP] Row {} failed: {}", rowNum, e.getMessage());
                FmUpdateMenuResultDTO.RowError err = new FmUpdateMenuResultDTO.RowError();
                err.setRowNumber(rowNum);
                err.setProductName(pName);
                err.setReason(e.getMessage());
                errors.add(err);
            }
        }

        log.info("[MENU-MAP] Map-update complete for outletId={}: updated={}, failed={}",
                outletId, productsUpdated, failureCount);

        FmUpdateMenuResultDTO result = new FmUpdateMenuResultDTO();
        result.setOutletId(outletId);
        result.setOutletName(outlet.getOutletName());
        result.setTotalRows(totalRows);
        result.setCategoriesCreated(0);
        result.setProductsCreated(0);
        result.setProductsUpdated(productsUpdated);
        result.setVariantsCreated(0);
        result.setFailureCount(failureCount);
        result.setErrors(errors);
        return result;
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    /**
     * Reads a required field from a row map, throwing if absent or blank.
     *
     * <p>Why throw: required fields like category and productName are meaningless
     * without a value. Throwing triggers the row-level error handler which
     * records the failure without aborting the whole upload.</p>
     *
     * @param row    the parsed row map
     * @param key    the normalised column key to look up
     * @param rowNum the 1-based row number for error messages
     * @return the trimmed non-blank value
     * @throws IllegalArgumentException if the field is missing or blank
     */
    private String required(Map<String, String> row, String key, int rowNum) {
        String v = get(row, key, null);
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("Missing required field '" + key + "' at row " + rowNum);
        return v.trim();
    }

    /**
     * Reads an optional field from the row map, falling back to a default value.
     *
     * <p>Why try both the keyed and underscore-stripped versions: column names
     * from Excel headers like "is_veg" and "isveg" should both map to the
     * same value. We normalise to no-underscore as the lookup key.</p>
     *
     * @param row the parsed row map
     * @param key the column key (with or without underscores)
     * @param def the fallback value if the key is absent or blank
     * @return the trimmed value or def
     */
    private String get(Map<String, String> row, String key, String def) {
        String v = row.get(key);
        // Also try the underscore-stripped key for flexibility with header formats
        if (v == null) v = row.get(key.replace("_", ""));
        return (v != null && !v.isBlank()) ? v.trim() : def;
    }

    /**
     * Parses a string into a {@link BigDecimal}, returning zero on failure.
     *
     * <p>Why strip non-numeric characters: Excel cells may contain currency
     * symbols or spaces (e.g. "₹ 99.50"). Stripping everything except
     * digits and the decimal point makes parsing robust.</p>
     *
     * @param s the raw price string from the cell
     * @return a valid {@link BigDecimal} or {@link BigDecimal#ZERO}
     */
    private BigDecimal parseBD(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(s.trim().replaceAll("[^0-9.]", "")); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    /**
     * Parses a veg/non-veg indicator string into a boolean.
     *
     * <p>Why multiple accepted values: different spreadsheet authors may write
     * "true", "yes", or "1". Accepting all three common representations avoids
     * import failures over formatting preferences.</p>
     *
     * @param s the cell value for the isVeg column
     * @return true if the value is "true", "yes", or "1" (case-insensitive)
     */
    private boolean parseVeg(String s) {
        return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s);
    }

    /**
     * Parses a variant string in the format {@code "Name:Price,Name:Price"} into
     * a list of name/price pairs.
     *
     * <p>Why this format: it keeps variants in a single cell which is easier
     * for outlet managers to fill in a spreadsheet than using multiple columns.</p>
     *
     * <p>Entries with blank name or price are skipped silently to tolerate
     * trailing commas or malformed entries in the cell.</p>
     *
     * @param s the raw variants cell value, e.g. "Small:99,Medium:149"
     * @return list of [name, price] string arrays (may be empty)
     */
    private List<String[]> parseVariants(String s) {
        List<String[]> result = new ArrayList<>();
        if (s == null || s.isBlank()) return result;
        for (String part : s.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2 && !kv[0].isBlank() && !kv[1].isBlank())
                result.add(new String[]{kv[0].trim(), kv[1].trim()});
        }
        return result;
    }

    /**
     * Capitalises the first letter of a string and lowercases the rest.
     *
     * <p>Why: category names from file uploads may be in all-caps or mixed-case.
     * Normalising to sentence-case ("Beverages" not "BEVERAGES") gives a
     * consistent look in the UI.</p>
     *
     * @param s the raw category name string
     * @return sentence-cased string, or the original if null/empty
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
