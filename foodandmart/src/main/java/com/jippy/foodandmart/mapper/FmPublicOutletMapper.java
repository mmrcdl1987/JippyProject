package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmPublicOutletDetailsDto;
import com.jippy.foodandmart.projections.FmPublicOutletDetailsProjection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FmPublicOutletMapper {

    private FmPublicOutletMapper() {
    }

    public static FmPublicOutletDetailsDto mapPublicOutletToDto(List<FmPublicOutletDetailsProjection> rows) {

        FmPublicOutletDetailsDto outlet = new FmPublicOutletDetailsDto();

        if (rows == null || rows.isEmpty()) {
            return outlet;
        }

        Map<Integer, FmPublicOutletDetailsDto.FmPublicCategoryDto> categoryMap = new LinkedHashMap<>();

        for (FmPublicOutletDetailsProjection row : rows) {

            // Set outlet details from first row
            if (outlet.getOutletId() == null) {
                outlet.setOutletId(row.getOutletId());
                outlet.setOutletName(row.getOutletName());
                outlet.setOutletAvailable(row.getOutletAvailable());
            }

            // Process category
            Integer categoryId = row.getCategoryId();
            if (categoryId != null) {
                FmPublicOutletDetailsDto.FmPublicCategoryDto category = categoryMap.computeIfAbsent(categoryId, id -> {
                    FmPublicOutletDetailsDto.FmPublicCategoryDto newCategory = new FmPublicOutletDetailsDto.FmPublicCategoryDto();
                    newCategory.setCategoryId(row.getCategoryId());
                    newCategory.setCategoryName(row.getCategoryName());
                    newCategory.setCategoryAvailable(row.getCategoryAvailable());
                    newCategory.setProducts(new ArrayList<>());
                    return newCategory;
                });

                // Process product
                Integer productId = row.getProductId();
                if (productId != null) {
                    FmPublicOutletDetailsDto.FmPublicProductDto product = findOrCreateProduct(category, row);
                    
                    // Process variant
                    Integer variantId = row.getProductVariantId();
                    if (variantId != null && product != null) {
                        FmPublicOutletDetailsDto.FmPublicProductVariantDto variant = new FmPublicOutletDetailsDto.FmPublicProductVariantDto();
                        variant.setProductVariantId(row.getProductVariantId());
                        variant.setVariantMerchantPrice(row.getVariantMerchantPrice());
                        variant.setVariantPriceType(row.getVariantPriceType());
                        variant.setVariantValueId(row.getVariantValueId());
                        variant.setVariantName(row.getVariantName());
                        variant.setVariantGroupId(row.getVariantGroupId());
                        variant.setVariantGroupName(row.getVariantGroupName());
                        variant.setVariantMinSelection(row.getVariantMinSelection());
                        variant.setVariantMaxSelection(row.getVariantMaxSelection());
                        
                        if (product.getVariants() == null) {
                            product.setVariants(new ArrayList<>());
                        }
                        product.getVariants().add(variant);
                    }
                }
            }
        }

        outlet.setCategories(new ArrayList<>(categoryMap.values()));
        return outlet;
    }

    private static FmPublicOutletDetailsDto.FmPublicProductDto findOrCreateProduct(
            FmPublicOutletDetailsDto.FmPublicCategoryDto category, 
            FmPublicOutletDetailsProjection row) {
        
        if (category.getProducts() != null) {
            for (FmPublicOutletDetailsDto.FmPublicProductDto existingProduct : category.getProducts()) {
                if (existingProduct.getProductId() != null && existingProduct.getProductId().equals(row.getProductId())) {
                    return existingProduct;
                }
            }
        }
        
        FmPublicOutletDetailsDto.FmPublicProductDto newProduct = new FmPublicOutletDetailsDto.FmPublicProductDto();
        newProduct.setProductId(row.getProductId());
        newProduct.setProductName(row.getProductName());
        newProduct.setDescription(row.getDescription());
        newProduct.setOnlinePrice(row.getOnlinePrice());
        newProduct.setIsVeg(row.getIsVeg());
        newProduct.setImageLink(row.getImageLink());
        newProduct.setHasProductVariants(row.getHasProductVariants());
        newProduct.setProductAvailable(row.getProductAvailable());
        newProduct.setVariants(new ArrayList<>());
        
        if (category.getProducts() == null) {
            category.setProducts(new ArrayList<>());
        }
        category.getProducts().add(newProduct);
        return newProduct;
    }
}
