package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_mapping_outlets_products", schema = "jippy_division")
@Getter
@Setter
public class DivCouponMappingOutletProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_mapping_id")
    private Integer couponMappingId;

    @Column(name = "coupon_id")
    private Integer couponId;

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

    @Column(name = "promotion_message")
    private String promotionMessage;

    @Column(name = "max_selection", nullable = false)
    private Integer maxSelection = -1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}