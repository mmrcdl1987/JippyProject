package com.jippy.foodandmart.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "specialized_outlets", schema = "jippy_fm")
@Getter
@Setter
public class FmSpecializedOutlet {

    @Id
    @Column(name = "specialized_outlet_id")
    private Integer specializedOutletId;

    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "city_id")
    private Integer cityId;

    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}