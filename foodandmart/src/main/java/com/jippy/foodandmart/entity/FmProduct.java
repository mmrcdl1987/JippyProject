package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "outlet_category_id")
    private Integer outletCategoryId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "outlet_category_id",
            insertable = false,
            updatable = false
    )
    private FmOutletCategory outletCategory;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    /**
     * Source price for base product pricing calculation.
     *
     * IMPORTANT:
     * Never use product_online_pricing.online_price
     * as the calculation source.
     */
    @Column(
            name = "merchant_price",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal merchantPrice;

    @Column(name = "is_veg", nullable = false)
    private Boolean isVeg = true;

    @Column(name = "has_product_variants", nullable = false)
    private Boolean hasProductVariants = false;

    @Column(name = "image_link")
    private String imageLink;

    @Column(name = "is_image_desc_updated", nullable = false)
    private Boolean isImageDescUpdated = Boolean.FALSE;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_active", length = 1)
    private String isActive = "Y";

    @Column(name = "is_toggle")
    private Boolean isToggle;


    @Column(name = "product_type", length = 20)
    private String productType;

    @JsonIgnore
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<FmProductVariant> variants = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}