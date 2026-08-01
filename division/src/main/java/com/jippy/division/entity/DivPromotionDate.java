package com.jippy.division.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_date", schema = "jippy_division")
@Getter
@Setter
public class DivPromotionDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_date_id")
    private Integer promotionDateId;

    @Column(name = "promotion_from_date")
    private LocalDateTime promotionFromDate;

    @Column(name = "promotion_to_date")
    private LocalDateTime promotionToDate;

    @Column(name = "no_of_slots")
    private Integer noOfSlots;

    @Column(name = "meal_type_slot_id")
    private Integer mealTypeSlotId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}