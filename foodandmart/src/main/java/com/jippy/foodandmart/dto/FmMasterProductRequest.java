package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FmMasterProductRequest {

    /**
     * Master Product Name
     * <p>
     * Excel:
     * master_product_name
     */
    private String masterProductName;

    /**
     * Product Description
     * <p>
     * Excel:
     * description
     */
    private String description;

    /**
     * Product Image URL
     * <p>
     * Excel:
     * photo
     */
    private String photo;

    /**
     * Category ID
     * <p>
     * Excel:
     * category_id
     */
    private Integer categoryId;

    /**
     * Category Name
     * <p>
     * Excel:
     * category_name
     */
    private String categoryName;

    /**
     * Veg / Non-Veg
     * <p>
     * Preferred Excel:
     * is_veg = true / false
     * <p>
     * Existing Excel:
     * veg = 1, non_veg = 0
     * <p>
     * Database:
     * jippy_fm.master_products.is_veg
     */
    private Boolean isVeg;

    /**
     * Cuisine Type
     * <p>
     * Excel:
     * cuisine_type
     */
    private String cuisineType;

    /**
     * Indicates whether the product has options.
     * <p>
     * Excel:
     * has_options
     * <p>
     * 0 = No
     * 1 = Yes
     */
    private Integer hasOptions;

    /**
     * Product options.
     * <p>
     * Excel:
     * options
     * <p>
     * Database:
     * JSONB
     */
    private String options;

    /**
     * Product Type
     * <p>
     * Excel:
     * product_type
     * <p>
     * Database:
     * jippy_fm.master_products.product_type
     */
    private String productType;

    /**
     * User who created the product.
     * <p>
     * This is system/request information,
     * not product information from Excel.
     */
    private Integer createdBy;

    /**
     * User who updated the product.
     * <p>
     * Normally null during initial creation.
     */
    private Integer updatedBy;
}