package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmGlobalSearchResultDTO;
import com.jippy.foodandmart.dto.FmGlobalSearchResultDTO.SearchItem;
import com.jippy.foodandmart.repository.FmMasterProductRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * REST controller for the global cross-entity search.
 * <p>
 * GET /api/fm/search?q=keyword
 * <p>
 * Searches across:
 * - Merchants
 * - Outlets
 * - Master Products
 * <p>
 * Master Product food type now uses:
 * <p>
 * isVeg = true  -> Veg
 * isVeg = false -> Non-Veg
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm/search")
public class FmGlobalSearchController {

    /**
     * Maximum results returned per entity section.
     */
    private static final int MAX_PER_SECTION = 5;

    private final FmMerchantRepository merchantRepository;

    private final FmOutletRepository outletRepository;

    private final FmMasterProductRepository masterProductRepository;


    /**
     * Executes a cross-entity text search and returns
     * results grouped by type.
     *
     * @param q search keyword
     * @return grouped search results
     */
    @GetMapping
    public ResponseEntity<FmApiResponse<FmGlobalSearchResultDTO>> search(@RequestParam(value = "q", defaultValue = "") String q) {

        String kw = q == null ? "" : q.trim().toLowerCase();

        log.info("[SEARCH] GET /api/fm/search?q={}", kw);


        // ============================================================
        // EMPTY SEARCH
        // ============================================================

        if (kw.isEmpty()) {

            FmGlobalSearchResultDTO empty = new FmGlobalSearchResultDTO();

            empty.setKeyword(q);

            empty.setTotalResults(0);

            empty.setMerchants(List.of());

            empty.setOutlets(List.of());

            empty.setMasterProducts(List.of());

            return ResponseEntity.ok(FmApiResponse.success("No keyword provided", empty));
        }


        // ============================================================
        // MERCHANTS
        // ============================================================

        List<SearchItem> merchants = merchantRepository.findAll().stream()

                .filter(m -> matches(kw, m.getMerchantName(), m.getMerchantEmail(), m.getMerchantPhone(), m.getMerchantBusinessType()))

                .limit(MAX_PER_SECTION)

                .map(m -> {

                    SearchItem item = new SearchItem();

                    item.setId(m.getMerchantId());

                    item.setTitle(m.getMerchantName() != null ? m.getMerchantName() : "—");

                    item.setSubtitle(m.getMerchantEmail());

                    item.setBadge(m.getStatus() != null ? m.getStatus() : "PENDING");

                    item.setSection("merchant");

                    return item;
                })

                .collect(Collectors.toList());


        // ============================================================
        // OUTLETS
        // ============================================================

        List<SearchItem> outlets = outletRepository.findAll().stream()

                .filter(o -> matches(kw, o.getOutletName(), cuisineTypeToString(o.getCuisineType()), o.getOutletPhone()))

                .limit(MAX_PER_SECTION)

                .map(o -> {

                    SearchItem item = new SearchItem();

                    item.setId(o.getOutletId());

                    item.setTitle(o.getOutletName());

                    item.setSubtitle(cuisineTypeToString(o.getCuisineType()));

                    item.setBadge("ID: " + o.getOutletId());

                    item.setSection("outlet");

                    return item;
                })

                .collect(Collectors.toList());


        // ============================================================
        // MASTER PRODUCTS
        // ============================================================

        /*
         * Uses DB-side search.
         *
         * Food type is now read from:
         *
         * FmMasterProduct.isVeg
         *
         * true  -> Veg
         * false -> Non-Veg
         */
        List<SearchItem> products = masterProductRepository.searchByName(kw).stream()

                .limit(MAX_PER_SECTION)

                .map(p -> {

                    SearchItem item = new SearchItem();

                    // ------------------------------------------------
                    // ID
                    // ------------------------------------------------

                    item.setId(p.getMasterProductId());


                    // ------------------------------------------------
                    // PRODUCT NAME
                    // ------------------------------------------------

                    item.setTitle(p.getMasterProductName());


                    // ------------------------------------------------
                    // DESCRIPTION
                    // ------------------------------------------------

                    String description = p.getDescription();

                    if (description == null) {

                        item.setSubtitle("");

                    } else if (description.length() > 60) {

                        item.setSubtitle(description.substring(0, 60) + "…");

                    } else {

                        item.setSubtitle(description);
                    }


                    // ------------------------------------------------
                    // VEG / NON-VEG
                    // ------------------------------------------------

                    /*
                     * New database field:
                     *
                     * is_veg boolean
                     *
                     * true  = Veg
                     * false = Non-Veg
                     */
                    item.setBadge(Boolean.TRUE.equals(p.getIsVeg()) ? "🟢 Veg" : "🔴 Non-Veg");


                    // ------------------------------------------------
                    // SECTION
                    // ------------------------------------------------

                    item.setSection("master-product");

                    return item;
                })

                .collect(Collectors.toList());


        // ============================================================
        // TOTAL
        // ============================================================

        int total = merchants.size() + outlets.size() + products.size();


        // ============================================================
        // RESPONSE
        // ============================================================

        FmGlobalSearchResultDTO result = new FmGlobalSearchResultDTO();

        result.setKeyword(q);

        result.setTotalResults(total);

        result.setMerchants(merchants);

        result.setOutlets(outlets);

        result.setMasterProducts(products);


        log.info("[SEARCH] keyword='{}' " + "→ merchants={}, outlets={}, products={}", kw, merchants.size(), outlets.size(), products.size());


        return ResponseEntity.ok(FmApiResponse.success(total + " results found", result));
    }


    // ============================================================
    // MATCH SEARCH
    // ============================================================

    /**
     * Returns true if any provided string field contains
     * the search keyword.
     */
    private boolean matches(String kw, String... fields) {

        if (kw == null || kw.isEmpty()) {
            return false;
        }

        if (fields == null) {
            return false;
        }

        for (String field : fields) {

            if (field != null && field.toLowerCase().contains(kw)) {

                return true;
            }
        }

        return false;
    }


    // ============================================================
    // CUISINE TYPE
    // ============================================================

    private String cuisineTypeToString(Integer[] cuisineType) {

        if (cuisineType == null || cuisineType.length == 0) {

            return "";
        }

        return Arrays.stream(cuisineType).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining(", "));
    }
}