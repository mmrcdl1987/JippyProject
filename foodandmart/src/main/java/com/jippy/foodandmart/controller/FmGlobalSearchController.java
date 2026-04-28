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

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for the global cross-entity search.
 *
 * <p>GET /api/search?q=keyword</p>
 *
 * <p>Searches across Merchants, Outlets, and Master Products using a
 * case-insensitive "contains" match on the most relevant text fields.
 * Returns up to {@link #MAX_PER_SECTION} results per entity type.</p>
 *
 * <p>Why a dedicated search controller: global search cuts across three
 * different entity types and their repositories. Putting this in any one
 * domain service would create inappropriate cross-service dependencies.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class FmGlobalSearchController {

    /** Maximum results returned per entity section to keep payloads small. */
    private static final int MAX_PER_SECTION = 5;

    private final FmMerchantRepository merchantRepository;
    private final FmOutletRepository outletRepository;
    private final FmMasterProductRepository masterProductRepository;

    /**
     * Executes a cross-entity text search and returns results grouped by type.
     *
     * <p>Why three separate filtered streams: each entity has different
     * searchable fields. Merchants are matched on name, email, phone, and
     * business type; outlets on name, cuisine, and phone; master products
     * use a dedicated DB-side full-text search method.</p>
     *
     * <p>Why limit per section: an unbounded search on a large dataset could
     * return thousands of results. Limiting to 5 per section keeps the UI
     * dropdown fast and readable.</p>
     *
     * @param q the search keyword (may be empty; empty keyword returns empty result)
     * @return an {@link FmApiResponse} wrapping a {@link FmGlobalSearchResultDTO}
     */
    @GetMapping
    public ResponseEntity<FmApiResponse<FmGlobalSearchResultDTO>> search(
            @RequestParam(value = "q", defaultValue = "") String q) {

        String kw = q.trim().toLowerCase();
        log.info("[SEARCH] GET /api/search?q={}", kw);

        if (kw.isEmpty()) {
            // Return an empty result instead of searching — empty keyword matches everything
            FmGlobalSearchResultDTO empty = new FmGlobalSearchResultDTO();
            empty.setKeyword(q);
            empty.setTotalResults(0);
            empty.setMerchants(List.of());
            empty.setOutlets(List.of());
            empty.setMasterProducts(List.of());
            return ResponseEntity.ok(FmApiResponse.success("No keyword provided", empty));
        }

        // ── Merchants ────────────────────────────────────────────────────────
        List<SearchItem> merchants = merchantRepository.findAll().stream()
                .filter(m -> matches(kw,
                        m.getMerchantName(),
                        m.getMerchantEmail(),
                        m.getMerchantPhone(),
                        m.getMerchantBusinessType()))
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

        // ── Outlets ──────────────────────────────────────────────────────────
        List<SearchItem> outlets = outletRepository.findAll().stream()
                .filter(o -> matches(kw,
                        o.getOutletName(),
                        o.getCuisineType(),
                        o.getOutletPhone()))
                .limit(MAX_PER_SECTION)
                .map(o -> {
                    SearchItem item = new SearchItem();
                    item.setId(o.getOutletId());
                    item.setTitle(o.getOutletName());
                    item.setSubtitle(o.getCuisineType());
                    item.setBadge("ID: " + o.getOutletId());
                    item.setSection("outlet");
                    return item;
                })
                .collect(Collectors.toList());

        // ── Master Products ──────────────────────────────────────────────────
        // Uses DB-side search (ILIKE) for efficiency instead of in-memory filtering
        List<SearchItem> products = masterProductRepository.searchByName(kw).stream()
                .limit(MAX_PER_SECTION)
                .map(p -> {
                    SearchItem item = new SearchItem();
                    item.setId(p.getMasterProductId());
                    item.setTitle(p.getMasterProductName());
                    // Truncate long descriptions to 60 chars for the search dropdown
                    item.setSubtitle(p.getDescription() != null
                            ? (p.getDescription().length() > 60
                                ? p.getDescription().substring(0, 60) + "…"
                                : p.getDescription())
                            : "");
                    item.setBadge(p.getVeg() == 1 ? "🟢 Veg" : "🔴 Non-Veg");
                    item.setSection("master-product");
                    return item;
                })
                .collect(Collectors.toList());

        int total = merchants.size() + outlets.size() + products.size();

        FmGlobalSearchResultDTO result = new FmGlobalSearchResultDTO();
        result.setKeyword(q);
        result.setTotalResults(total);
        result.setMerchants(merchants);
        result.setOutlets(outlets);
        result.setMasterProducts(products);

        log.info("[SEARCH] keyword='{}' → merchants={}, outlets={}, products={}",
                kw, merchants.size(), outlets.size(), products.size());

        return ResponseEntity.ok(FmApiResponse.success(total + " results found", result));
    }

    /**
     * Returns true if any of the provided string fields contains the keyword.
     *
     * <p>Why varargs: the number of searchable fields differs between entity
     * types. A varargs helper avoids one {@code matches()} method per entity.</p>
     *
     * @param kw     the lowercase search keyword
     * @param fields the entity fields to check (nulls are skipped safely)
     * @return true if at least one field contains the keyword
     */
    private boolean matches(String kw, String... fields) {
        for (String f : fields) {
            if (f != null && f.toLowerCase().contains(kw)) return true;
        }
        return false;
    }
}
