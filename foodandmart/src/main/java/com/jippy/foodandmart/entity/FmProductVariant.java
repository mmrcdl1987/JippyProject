package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_variant_id")
    private Integer productVariantId;

    @Column(name = "product_id", nullable = false, insertable = true, updatable = true)
    private Integer productId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private FmProduct product;

    @Column(name = "variant_name", length = 100, nullable = false)
    private String variantName;
    @Column(name = "merchant_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal merchantPrice;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
