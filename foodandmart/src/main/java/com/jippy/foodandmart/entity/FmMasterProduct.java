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

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "master_products", schema = "jippy_fm")
public class FmMasterProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_product_id")
    private Integer masterProductId;

    @NotBlank(message = "Master product name is required")
    @Size(max = 100)
    @Column(name = "master_product_name", nullable = false, length = 100)
    private String masterProductName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;

    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "sub_category_id")
    private Integer subCategoryId;

    @Column(name = "sub_category_name", length = 100)
    private String subCategoryName;

    @Column(name = "has_options", nullable = false)
    @Builder.Default
    private Integer hasOptions = 0;

    @Column(name = "options_enabled", nullable = false)
    @Builder.Default
    private Integer optionsEnabled = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private String options;

    @Column(name = "veg", nullable = false)
    @Builder.Default
    private Integer veg = 0;

    @Column(name = "non_veg", nullable = false)
    @Builder.Default
    private Integer nonVeg = 0;

    @Column(name = "food_type", length = 50)
    private String foodType;

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;

    @Column(name = "calories")
    @Builder.Default
    private Integer calories = 0;

    @Column(name = "protein")
    @Builder.Default
    private Integer protein = 0;

    @Column(name = "fats")
    @Builder.Default
    private Integer fats = 0;

    @Column(name = "carbs")
    @Builder.Default
    private Integer carbs = 0;

    @Column(name = "grams")
    @Builder.Default
    private Integer grams = 0;

    @Column(name = "publish", nullable = false)
    @Builder.Default
    private Integer publish = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    /** Transient — carries the merchant_price from an uploaded CSV file.
     *  Never persisted to the master_products table. */
    @Transient
    private Double csvMerchantPrice;

    /** Transient — carries the raw availability timing string (e.g. "9:00-22:00")
     *  from an uploaded CSV file. Never persisted to master_products. */
    @Transient
    private String csvTiming;

    /** Transient - carries the day-of-week name (e.g. "Monday", "Sunday")
     *  from an uploaded CSV file. Never persisted to master_products. */
    @Transient
    private String csvDayOfWeek;
}
