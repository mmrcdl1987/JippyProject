package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity for jippy_fm.area.
 *
 * <p>Each area belongs to a city. During outlet bulk upload the area is looked up
 * by name so only the integer area_id needs to be stored in the address table.</p>
 */
@Entity
@Table(name = "area", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Integer areaId;

    @Column(name = "area_name", length = 50)
    private String areaName;

    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    @Column(name = "created_at")  private LocalDateTime createdAt;
    @Column(name = "created_by")  private Integer createdBy;
    @Column(name = "updated_at")  private LocalDateTime updatedAt;
    @Column(name = "updated_by")  private Integer updatedBy;
}
