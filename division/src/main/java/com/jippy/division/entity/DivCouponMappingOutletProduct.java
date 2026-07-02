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

    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "promotion_time_id")
    private Integer promotionTimeId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}