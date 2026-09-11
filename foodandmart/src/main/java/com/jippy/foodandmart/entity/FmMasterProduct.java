package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "master_products", schema = "jippy_fm")
public class FmMasterProduct {

    /**
     * Primary Key
     * <p>
     * The database should generate this value.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_product_id")
    private Integer masterProductId;

    /**
     * Master Product Name
     */
    @NotBlank(message = "Master product name is required")
    @Size(max = 100)
    @Column(name = "master_product_name", nullable = false, length = 100)
    private String masterProductName;

    /**
     * Product Description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Product Image
     */
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;

    /**
     * Category ID
     */
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    /**
     * Category Name
     */
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /**
     * Veg / Non-Veg
     * <p>
     * true  = Veg
     * false = Non-Veg
     */
    @Column(name = "is_veg", nullable = false)
    private Boolean isVeg;

    /**
     * Cuisine Type
     */
    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    /**
     * Indicates whether the product has options.
     * <p>
     * 0 = No
     * 1 = Yes
     */
    @Column(name = "has_options", nullable = false)
    @Builder.Default
    private Integer hasOptions = 0;

    /**
     * Product options stored as JSONB.
     * <p>
     * Example:
     * <p>
     * [
     * {
     * "name": "Size",
     * "values": [
     * "Small",
     * "Medium",
     * "Large"
     * ]
     * }
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private String options;

    /**
     * Product Type
     */
    @Column(name = "product_type", nullable = false, length = 10)
    private String productType;

    /**
     * Active Status
     * <p>
     * Y = Active
     * N = Inactive
     */
    @Column(name = "is_active", nullable = false, length = 1)
    @Builder.Default
    private String isActive = "Y";

    /**
     * Created Timestamp
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Created By
     */
    @Column(name = "created_by")
    private Integer createdBy;

    /**
     * Updated Timestamp
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Updated By
     */
    @Column(name = "updated_by")
    private Integer updatedBy;


    // ============================================================
    // CSV / EXCEL TEMPORARY VALUES
    // ============================================================

    /**
     * Merchant price from uploaded CSV/Excel.
     * <p>
     * IMPORTANT:
     * <p>
     * This field is NOT stored in master_products.
     * It is only used temporarily during:
     * <p>
     * CSV -> Compare -> Add To Outlet Products
     */
    @Transient
    private Double csvMerchantPrice;

    /**
     * Timing from uploaded CSV/Excel.
     * <p>
     * IMPORTANT:
     * <p>
     * This field is NOT stored in master_products.
     */
    @Transient
    private String csvTiming;

    /**
     * Day of week from uploaded CSV/Excel.
     * <p>
     * IMPORTANT:
     * <p>
     * This field is NOT stored in master_products.
     */
    @Transient
    private String csvDayOfWeek;
}