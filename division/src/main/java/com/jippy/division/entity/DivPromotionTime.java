package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "promotion_time", schema = "jippy_division")
@Getter
@Setter
public class DivPromotionTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_time_id")
    private Integer promotionTimeId;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Column(name = "promotion_from_time")
    private LocalTime promotionFromTime;

    @Column(name = "promotion_to_time")
    private LocalTime promotionToTime;

    @Column(name = "promotion_date_id")
    private Integer promotionDateId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}