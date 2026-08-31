package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(name = "PublicOutletDetails", description = "Public outlet details with basic product information")
public class FmPublicOutletDetailsDto {

    @Schema(description = "Outlet ID")
    private Integer outletId;

    @Schema(description = "Outlet name")
    private String outletName;

    @Schema(description = "Outlet availability status")
    private Boolean outletAvailable;

    @Schema(description = "List of categories with products")
    private List<FmPublicCategoryDto> categories;

    @Data
    @Schema(name = "PublicCategory", description = "Public category details")
    public static class FmPublicCategoryDto {

        @Schema(description = "Category ID")
        private Integer categoryId;

        @Schema(description = "Category name")
        private String categoryName;

        @Schema(description = "Category availability status")
        private Boolean categoryAvailable;

        @Schema(description = "List of products in this category")
        private List<FmPublicProductDto> products;
    }

    @Data
    @Schema(name = "PublicProduct", description = "Public product details")
    public static class FmPublicProductDto {

        @Schema(description = "Product ID")
        private Integer productId;

        @Schema(description = "Product name")
        private String productName;

        @Schema(description = "Product description")
        private String description;

        @Schema(description = "Online price from product_online_pricing table")
        private BigDecimal onlinePrice;

        @Schema(description = "Is vegetarian")
        private Boolean isVeg;

        @Schema(description = "Product image URL")
        private String imageLink;

        @Schema(description = "Has product variants")
        private Boolean hasProductVariants;

        @Schema(description = "Product availability status")
        private Boolean productAvailable;

        @Schema(description = "Product variants")
        private List<FmPublicProductVariantDto> variants;
    }

    @Data
    @Schema(name = "PublicProductVariant", description = "Public product variant details")
    public static class FmPublicProductVariantDto {

        @Schema(description = "Product variant ID")
        private Integer productVariantId;

        @Schema(description = "Variant merchant price")
        private BigDecimal variantMerchantPrice;

        @Schema(description = "Variant price type")
        private String variantPriceType;

        @Schema(description = "Variant value ID")
        private Integer variantValueId;

        @Schema(description = "Variant name")
        private String variantName;

        @Schema(description = "Variant group ID")
        private Integer variantGroupId;

        @Schema(description = "Variant group name")
        private String variantGroupName;

        @Schema(description = "Variant min selection")
        private Integer variantMinSelection;

        @Schema(description = "Variant max selection")
        private Integer variantMaxSelection;
    }
}
