package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "address", schema = "jippy_fm")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmAddress {

    @Id
    @Column(name = "address_id")
    private Integer addressId;

    @Column(name = "jippy_address_id", nullable = false)
    private Integer jippyAddressId;

    @Column(name = "building_number", nullable = false)
    private String buildingNumber;

    @Column(name = "road", nullable = false)
    private String road;

    @Column(name = "landmark", nullable = false)
    private String landmark;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "state_id", nullable = false)
    private Integer stateId;

    @Column(name = "area_id", nullable = false)
    private Integer areaId;

    @Column(name = "address_type", nullable = false)
    private String addressType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

}