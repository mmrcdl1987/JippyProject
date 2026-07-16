package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "week_slot_days", schema = "jippy_fm")
@Getter
@Setter
public class BannerSlotDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "week_slot_days_id")
    private Integer bannerSlotDaysId;

    @Column(name = "slot_start_date", nullable = false)
    private LocalDate slotStartDate;

    @Column(name = "slot_end_date", nullable = false)
    private LocalDate slotEndDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "week_name")
    private String weekName;

    @Column(name = "slot_type")
    private String slotType;
}