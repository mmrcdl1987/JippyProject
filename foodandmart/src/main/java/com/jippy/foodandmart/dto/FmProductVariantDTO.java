package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmProductVariantDTO {

    /**
     * product_variant_options.product_variant_options_id
     */
    @Schema(example = "12345", description = "Unique identifier for the product variant option.")
    private Integer variantId;

    /**
     * product_variant_group_values.product_variant_group_values_id
     */
//    private Integer variantValueId;

    /**
     * product_variant_groups.product_variant_groups_id
     */
//    private Integer variantGroupId;

    /**
     * Variant group name.
     * Example: Size, Portion, Extras
     */
    @Schema(example = "Size", description = "Name of the variant group, e.g., Size, Portion, Extras.")
    private String groupName;

    /**
     * Variant value/display name.
     * Example: Small, Medium, Large, Extra Cheese
     */
    @Schema(example = "Small", description = "Display name of the variant value, e.g., Small, Medium, Large, Extra Cheese.")
    private String variantName;

    /**
     * SINGLE / MULTIPLE
     */
//    @Schema(example = "SINGLE", description = "Indicates whether the selection type is SINGLE or MULTIPLE.")
//    private String selectionType;

    /**
     * Minimum number of selections allowed.
     */
//    private Integer minSelection;

    /**
     * Maximum number of selections allowed.
     */
//    private Integer maxSelection;

    /**
     * MAIN / ADD
     */
    @Schema(example = "MAIN", description = "Indicates whether the price type is MAIN or ADD.")
    private String priceType;

    /**
     * Merchant/source price from
     * product_variant_options.variant_price
     */
    @Schema(example = "5.99", description = "Merchant/source price for the variant option.")
    private BigDecimal merchantPrice;

    /**
     * Final online price.
     *
     * This will come from product_online_pricing
     * if your pricing table contains variant-specific
     * pricing. Otherwise it should not be populated from
     * the product-level online price.
     */
    @Schema(example = "6.49", description = "Final online price for the variant option, if applicable.")
    private BigDecimal onlinePrice;}