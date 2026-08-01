package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmMenuCopyRequestDTO;
import com.jippy.foodandmart.dto.FmMenuCopyResultDTO;
import com.jippy.foodandmart.dto.FmMenuItemDTO;
import com.jippy.foodandmart.dto.FmOutletSummaryDTO;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletCategory;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductVariant;
import com.jippy.foodandmart.repository.FmOutletCategoryRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.repository.FmProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jippy.foodandmart.service.IFmMenuService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for menu (product) operations.
 *
 * Copy logic:
 *   source outlet → outlet_categories → products
 *   For each destination outlet, find the matching outlet_category
 *   (same category_id). If a matching category exists in the destination,
 *   clone the product row with that destination outlet_category_id.
 *   If the destination does not have that category yet, we create a new
 *   outlet_category row first, then insert the product under it.
 *   Variants are copied along with each product.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FmMenuServiceImpl implements IFmMenuService {

    private final FmOutletRepository outletRepository;
    private final FmOutletCategoryRepository outletCategoryRepository;
    private final FmProductRepository productRepository;
    private final FmProductVariantRepository productVariantRepository;

    // ── List all outlets ──────────────────────────────────────────────────────

    @Override
    public List<FmOutletSummaryDTO> listAllOutlets() {
        return outletRepository.findAll().stream()
                .map(o -> FmOutletSummaryDTO.from(o, countProductsForOutlet(o.getOutletId())))
                .collect(Collectors.toList());
    }

    // ── Get products for a source outlet ─────────────────────────────────────

    @Override
    public List<FmMenuItemDTO> getMenuByOutlet(Integer outletId) {
        if (!outletRepository.existsById(outletId))
            throw new IllegalArgumentException("Outlet ID " + outletId + " does not exist");

        log.info("[MENU] getMenuByOutlet: outletId={}", outletId);

        return outletCategoryRepository.findByOutletId(outletId).stream()
                .flatMap(cat -> productRepository.findByOutletCategoryId(cat.getOutletCategoryId()).stream())
                .map(FmMenuItemDTO::fromProduct)
                .collect(Collectors.toList());
    }

    // ── Copy menu (products) to destination outlets ───────────────────────────

    /**
     * Copies selected products from the source outlet to one or more destination outlets.
     *
     * Strategy:
     * 1. Collect source outlet_categories  →  map: categoryId → outletCategoryId
     * 2. Fetch all products that belong to those categories (filtered by selected itemIds if any)
     * 3. For every destination outlet:
     *    a. Build a map: categoryId → dest outletCategoryId
     *       - If the dest outlet already has that categoryId, use its outletCategoryId
     *       - Otherwise create a new outlet_category row for the dest outlet
     *    b. For each source product, clone it under the matching dest outletCategoryId
     *       - Skip (or overwrite) if a product with the same name already exists there
     *    c. Copy variants for each newly inserted product
     */
    @Override
    @Transactional
    public FmMenuCopyResultDTO copyMenu(FmMenuCopyRequestDTO req) {

        Integer srcOutletId = req.getSourceOutletId();
        List<Integer> destOutletIds = req.getDestinationOutletIds();
        FmMenuCopyRequestDTO.CopyOptions opts = req.getOptions() != null
                ? req.getOptions() : new FmMenuCopyRequestDTO.CopyOptions();

        log.info("[MENU] copyMenu: sourceOutlet={}, destinations={}", srcOutletId, destOutletIds);

        // Validate source outlet
        outletRepository.findById(srcOutletId)
                .orElseThrow(() -> new IllegalArgumentException("Source outlet " + srcOutletId + " not found"));

        // Source: categoryId → outlet_category_id
        List<FmOutletCategory> srcCategories = outletCategoryRepository.findByOutletId(srcOutletId);
        Map<Integer, Integer> srcCatIdToOcId = srcCategories.stream()
                .collect(Collectors.toMap(FmOutletCategory::getCategoryId, FmOutletCategory::getOutletCategoryId));

        // All source outlet_category_ids
        List<Integer> srcOcIds = srcCategories.stream()
                .map(FmOutletCategory::getOutletCategoryId)
                .collect(Collectors.toList());

        // Fetch all products for the source outlet
        List<FmProduct> allSrcProducts = srcOcIds.stream()
                .flatMap(ocId -> productRepository.findByOutletCategoryId(ocId).stream())
                .collect(Collectors.toList());

        // Filter to selected products only (null/empty = copy ALL)
        List<FmProduct> productsToCopy;
        if (req.getMenuItemIds() == null || req.getMenuItemIds().isEmpty()) {
            productsToCopy = allSrcProducts;
        } else {
            Set<Integer> selectedIds = new HashSet<>(req.getMenuItemIds());
            productsToCopy = allSrcProducts.stream()
                    .filter(p -> selectedIds.contains(p.getProductId()))
                    .collect(Collectors.toList());
        }

        if (productsToCopy.isEmpty()) {
            FmMenuCopyResultDTO empty = new FmMenuCopyResultDTO();
            empty.setTotalItems(0);
            empty.setTotalOutlets(destOutletIds.size());
            empty.setSuccessCount(0);
            empty.setFailureCount(0);
            empty.setErrors(List.of());
            return empty;
        }

        // Prefetch variants for all source products in one query
        List<Integer> srcProductIds = productsToCopy.stream()
                .map(FmProduct::getProductId).collect(Collectors.toList());
        Map<Integer, List<FmProductVariant>> variantsByProductId =
                productVariantRepository.findByProductIdIn(srcProductIds).stream()
                        .collect(Collectors.groupingBy(FmProductVariant::getProductId));

        int successCount = 0;
        int failureCount = 0;
        List<FmMenuCopyResultDTO.CopyError> errors = new ArrayList<>();

        for (Integer destOutletId : destOutletIds) {

            Optional<FmOutlet> destOpt = outletRepository.findById(destOutletId);
            if (destOpt.isEmpty()) {
                // All products fail for this destination
                for (FmProduct p : productsToCopy) {
                    failureCount++;
                    errors.add(buildError(destOutletId, "Unknown", p, "Destination outlet not found"));
                }
                continue;
            }
            String destOutletName = destOpt.get().getOutletName();

            // Build dest categoryId → outlet_category_id map
            // Create missing categories on the fly
            Map<Integer, Integer> destCatIdToOcId = buildDestCategoryMap(
                    destOutletId, srcCatIdToOcId.keySet());

            // Now copy each product
            for (FmProduct src : productsToCopy) {
                // Find which categoryId this source product belongs to
                Integer srcOcId = src.getOutletCategoryId();
                Integer categoryId = srcCategories.stream()
                        .filter(c -> c.getOutletCategoryId().equals(srcOcId))
                        .map(FmOutletCategory::getCategoryId)
                        .findFirst().orElse(null);

                if (categoryId == null) {
                    failureCount++;
                    errors.add(buildError(destOutletId, destOutletName, src,
                            "Could not resolve category for source product"));
                    continue;
                }

                Integer destOcId = destCatIdToOcId.get(categoryId);
                if (destOcId == null) {
                    failureCount++;
                    errors.add(buildError(destOutletId, destOutletName, src,
                            "No matching outlet_category found at destination"));
                    continue;
                }

                try {
                    Integer newProductId = cloneProduct(src, destOcId, opts);
                    // Copy variants
                    List<FmProductVariant> srcVariants = variantsByProductId
                            .getOrDefault(src.getProductId(), List.of());
                    for (FmProductVariant sv : srcVariants) {
                        cloneVariant(sv, newProductId);
                    }
                    successCount++;
                    log.debug("[MENU] Copied product '{}' → destOutlet={}, destOcId={}",
                            src.getProductName(), destOutletId, destOcId);
                } catch (IllegalStateException e) {
                    // overwriteExisting=false and product already exists — skip silently counted as failure
                    failureCount++;
                    errors.add(buildError(destOutletId, destOutletName, src, e.getMessage()));
                } catch (Exception e) {
                    failureCount++;
                    errors.add(buildError(destOutletId, destOutletName, src, e.getMessage()));
                    log.error("[MENU] Error copying product '{}' to outlet {}: {}",
                            src.getProductName(), destOutletId, e.getMessage());
                }
            }
        }

        log.info("[MENU] Copy complete: success={}, failed={}", successCount, failureCount);

        FmMenuCopyResultDTO result = new FmMenuCopyResultDTO();
        result.setTotalItems(productsToCopy.size());
        result.setTotalOutlets(destOutletIds.size());
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setErrors(errors);
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a map of categoryId → outlet_category_id for the destination outlet.
     * If the destination outlet does not yet have a category that the source outlet has,
     * a new outlet_category row is created so the product can be inserted.
     */
    private Map<Integer, Integer> buildDestCategoryMap(Integer destOutletId,
                                                        Set<Integer> requiredCategoryIds) {
        // Existing dest categories
        List<FmOutletCategory> existing = outletCategoryRepository.findByOutletId(destOutletId);
        Map<Integer, Integer> map = new HashMap<>();
        for (FmOutletCategory oc : existing) {
            map.put(oc.getCategoryId(), oc.getOutletCategoryId());
        }

        // Create missing categories
        for (Integer catId : requiredCategoryIds) {
            if (!map.containsKey(catId)) {
                FmOutletCategory newOc = new FmOutletCategory();
                newOc.setOutletId(destOutletId);
                newOc.setCategoryId(catId);
                FmOutletCategory saved = outletCategoryRepository.save(newOc);
                map.put(catId, saved.getOutletCategoryId());
                log.info("[MENU] Created outlet_category for destOutlet={}, categoryId={}, ocId={}",
                        destOutletId, catId, saved.getOutletCategoryId());
            }
        }
        return map;
    }

    /**
     * Clones a single product into the destination outlet_category.
     * Respects overwriteExisting: if false and product name already exists → throws.
     * Returns the new (or existing overwritten) productId.
     */
    private Integer cloneProduct(FmProduct src, Integer destOcId,
                                 FmMenuCopyRequestDTO.CopyOptions opts) {

        Optional<FmProduct> existing = productRepository
                .findByOutletCategoryIdAndProductNameIgnoreCase(destOcId, src.getProductName());

        if (existing.isPresent()) {
            if (!opts.isOverwriteExisting())
                throw new IllegalStateException(
                        "Product '" + src.getProductName() + "' already exists at destination (overwrite=false)");

            // Overwrite: update fields on the existing product
            FmProduct ex = existing.get();
            ex.setDescription(src.getDescription());
            ex.setIsVeg(src.getIsVeg());
            ex.setHasProductVariants(src.getHasProductVariants());
            if (opts.isCopyPrices())  ex.setMerchantPrice(src.getMerchantPrice());
            if (opts.isCopyImages())  {
                ex.setImageLink(src.getImageLink());
//                ex.setPhotos(src.getPhotos());
//                ex.setThumbnail(src.getThumbnail());
            }
            return productRepository.save(ex).getProductId();
        }

        // New product clone
        FmProduct clone = new FmProduct();
        clone.setOutletCategoryId(destOcId);
        clone.setProductName(src.getProductName());
        clone.setDescription(src.getDescription());
        clone.setIsVeg(src.getIsVeg());
        clone.setHasProductVariants(src.getHasProductVariants());
        clone.setMerchantPrice(opts.isCopyPrices() ? src.getMerchantPrice() : java.math.BigDecimal.ZERO);
        clone.setImageLink(opts.isCopyImages() ? src.getImageLink() : null);
//        clone.setPhotos(opts.isCopyImages() ? src.getPhotos() : null);
//        clone.setThumbnail(opts.isCopyImages() ? src.getThumbnail() : null);
        return productRepository.save(clone).getProductId();
    }

    /**
     * Clones a single product variant under the newly copied product.
     */
    private void cloneVariant(FmProductVariant src, Integer newProductId) {
        FmProductVariant clone = new FmProductVariant();
        clone.setProductId(newProductId);
        clone.setVariantName(src.getVariantName());
        clone.setMerchantPrice(src.getMerchantPrice());
        productVariantRepository.save(clone);
    }

    /**
     * Counts total products linked to an outlet via outlet_categories.
     */
    private long countProductsForOutlet(Integer outletId) {
        return outletCategoryRepository.findByOutletId(outletId).stream()
                .mapToLong(cat -> productRepository.countByOutletCategoryId(cat.getOutletCategoryId()))
                .sum();
    }

    private FmMenuCopyResultDTO.CopyError buildError(Integer destOutletId, String destOutletName,
                                                      FmProduct p, String reason) {
        FmMenuCopyResultDTO.CopyError err = new FmMenuCopyResultDTO.CopyError();
        err.setDestOutletId(destOutletId);
        err.setDestOutletName(destOutletName);
        err.setItemId(p.getProductId());
        err.setItemName(p.getProductName());
        err.setReason(reason);
        return err;
    }
}
