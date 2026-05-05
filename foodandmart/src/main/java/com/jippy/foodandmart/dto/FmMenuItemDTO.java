package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jippy.foodandmart.entity.FmProduct;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a single menu item (product) for API responses.
 *
 * <p>Built from {@link FmProduct} — products are linked to outlets via
 * outlet_categories. The static factory {@link #fromProduct(FmProduct)} handles
 * the conversion so service code stays clean.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMenuItemDTO {

    /** Database primary key of the product. */
    private Integer itemId;

    /** The outlet_category_id this product belongs to (links product → outlet). */
    private Integer outletCategoryId;

    /** Display name of the product (e.g. "Masala Dosa"). */
    private String  itemName;

    /** Optional free-text description shown in the UI. */
    private String  description;

    /** Merchant price of the product in rupees. */
    private BigDecimal price;

    /** URL or base64 data URI of the product's photo. May be null. */
    private String  imageUrl;

    /** Whether the product is vegetarian. */
    private Boolean isVeg;

    /** Whether the product has multiple variants. */
    private Boolean hasProductVariants;

    /**
     * Converts a {@link FmProduct} entity to this DTO.
     *
     * @param p the product entity from the DB
     * @return a fully populated {@link FmMenuItemDTO}
     */
    public static FmMenuItemDTO fromProduct(FmProduct p) {
        FmMenuItemDTO dto = new FmMenuItemDTO();
        dto.setItemId(p.getProductId());
        dto.setOutletCategoryId(p.getOutletCategoryId());
        dto.setItemName(p.getProductName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getMerchantPrice());
        dto.setImageUrl(p.getImageLink());
        dto.setIsVeg(p.getIsVeg());
        dto.setHasProductVariants(p.getHasProductVariants());
        return dto;
    }
}
