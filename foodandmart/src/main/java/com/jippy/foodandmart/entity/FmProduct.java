package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
@Builder
public class FmProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "outlet_category_id", nullable = true, insertable = true, updatable = true)
    private Integer outletCategoryId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_category_id", insertable = false, updatable = false)
    private FmOutletCategory outletCategory;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;
    @Column(name = "description", length = 500, nullable = false)
    private String description;
    @Column(name = "merchant_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal merchantPrice;
    @Column(name = "is_veg", nullable = false)
    @Builder.Default
    private Boolean isVeg = true;
    @Column(name = "has_product_variants", nullable = false)
    @Builder.Default
    private Boolean hasProductVariants = false;
//    @Column(name = "image_link",          length = 500) private String imageLink;
@Column(name = "image_link", columnDefinition = "TEXT") private String imageLink;
    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;
    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_active", length = 1)
    @Builder.Default
    private String isActive = "Y";

    private Boolean isToggle;

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
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
