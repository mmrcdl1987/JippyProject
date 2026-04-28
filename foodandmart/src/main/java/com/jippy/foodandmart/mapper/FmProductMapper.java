package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmProductDto;
import com.jippy.foodandmart.dto.FmProductVariantDTO;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.entity.FmProductVariant;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility class for converting between {@link FmProductDto} /
 * {@link FmProductVariantDTO} and the {@link FmProduct} / {@link FmProductVariant} entities.
 *
 * <p>Why a combined mapper: product and variant conversions are always used
 * together (a product DTO always carries its variants). Keeping them in one
 * mapper avoids a tiny separate VariantMapper file.</p>
 */
public  class FmProductMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmProductMapper() {}
    public  static Map<String,Double> priceMapper = new HashMap<String,Double>();
    /** Maps product name -> day-of-week name (e.g. "Monday") from the CSV daysofaweek column. */
    public static Map<String,String> dayOfWeekMapper = new HashMap<String,String>();
    /** Maps product name -> raw CSV timing string (e.g. "9:00-22:00"). */
    public  static Map<String,String> timingMapper = new HashMap<String,String>();

    /**
     * Converts a {@link FmProductDto} into a new {@link FmProduct} entity.
     *
     * <p>Why accept outletCategoryId and createdBy as separate parameters:
     * these values are not part of the DTO (they come from the URL path and
     * the session/audit context). Passing them explicitly keeps the DTO
     * free of context-specific fields.</p>
     *
     * <p>Why default isVeg to true and hasProductVariants to false:
     * these are the most common values. Explicit false values must be
     * sent in the request; absence means "use default".</p>
     *
     * @param dto              the product data from the API request
     * @param outletCategoryId the FK linking this product to an outlet category
     * @param createdBy        the user ID for the audit trail
     * @return a transient {@link FmProduct} entity ready to persist
     */
    public static FmProduct toEntity(FmProductDto dto, Integer outletCategoryId, Integer createdBy) {
        FmProduct entity = new FmProduct();
        entity.setProductName(dto.getProductName() != null ? dto.getProductName().trim() : null);
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        entity.setMerchantPrice(dto.getMerchantPrice());
        // Default to veg=true when the flag is not provided
        entity.setIsVeg(dto.getIsVeg() != null ? dto.getIsVeg() : true);
        // Default to no variants — the flag is set to true only when variant entries are present
        entity.setHasProductVariants(dto.getHasProductVariants() != null ? dto.getHasProductVariants() : false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    /**
     * Converts a {@link FmProductVariantDTO} into a new {@link FmProductVariant} entity.
     *
     * <p>Why productId and createdBy are separate parameters: same reason as
     * in {@link #toEntity} — they come from context, not from the DTO itself.</p>
     *
     * @param dto       the variant data from the API request
     * @param productId the FK linking this variant to its parent product
     * @param createdBy the user ID for the audit trail
     * @return a transient {@link FmProductVariant} entity ready to persist
     */
    public static FmProductVariant toVariantEntity(FmProductVariantDTO dto, Integer productId, Integer createdBy) {
        FmProductVariant entity = new FmProductVariant();
        entity.setProductId(productId);
        entity.setVariantName(dto.getVariantName() != null ? dto.getVariantName().trim() : null);
        entity.setMerchantPrice(dto.getMerchantPrice());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    /**
     * Converts a {@link FmProduct} entity into a {@link FmProductDto} with nested variants.
     *
     * <p>Why load variants here: the product list endpoint returns products
     * with their variants nested. Loading them inside the mapper keeps the
     * service call simple — it just maps the entity without knowing the
     * variant loading detail.</p>
     *
     * <p>Why return an empty list instead of null for variants: a null variants
     * field would require every frontend consumer to null-check before iterating.
     * An empty list is safer and cleaner for JavaScript consumers.</p>
     *
     * @param product the persisted product entity (variants collection should be loaded)
     * @return a {@link FmProductDto} with a nested (possibly empty) variants list
     */
    public static FmProductDto toDTO(FmProduct product) {
        FmProductDto dto = new FmProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setMerchantPrice(product.getMerchantPrice());
        dto.setIsVeg(product.getIsVeg());
        dto.setHasProductVariants(product.getHasProductVariants());
        // Map variants; return empty list if the association was not loaded
        List<FmProductVariantDTO> variants = product.getVariants() != null
                ? product.getVariants().stream().map(FmProductMapper::toVariantDTO).collect(Collectors.toList())
                : Collections.emptyList();
        dto.setVariants(variants);
        return dto;
    }

    /**
     * Converts a {@link FmProductVariant} entity into a {@link FmProductVariantDTO}.
     *
     * <p>Why a separate method: allows streaming conversion in
     * {@link #toDTO(FmProduct)} via method reference without an inline lambda.</p>
     *
     * @param variant the persisted variant entity
     * @return a {@link FmProductVariantDTO} safe for JSON serialisation
     */
    public static FmProductVariantDTO toVariantDTO(FmProductVariant variant) {
        FmProductVariantDTO dto = new FmProductVariantDTO();
        dto.setVariantId(variant.getProductVariantId());
        dto.setVariantName(variant.getVariantName());
        dto.setMerchantPrice(variant.getMerchantPrice());
        return dto;
    }
}
