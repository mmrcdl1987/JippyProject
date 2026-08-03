package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_drop_mapping_outlets_products", schema = "jippy_division")
@Getter
@Setter
public class DivPriceDropMappingOutletsProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_drop_mapping_outlets_products_id")
    private Integer priceDropMappingOutletsProductsId;

    @Column(name = "price_drop_value")
    private Double priceDropValue;

    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "location_type")
    private String locationType;

    @Column(name = "promotion_date_id")
    private Integer promotionDateId;

    @Column(name = "price_model_id")
    private Integer priceModelId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}