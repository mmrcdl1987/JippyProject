package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_streaks", schema = "jippy_customer_and_order")
public class CoCustomerStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_streak_id")
    private Integer customerStreakId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "current_streak")
    private Integer currentStreak;

//    @Column(name = "max_streak")
//    private Integer maxStreak;

    @Column(name = "points")
    private Integer points;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}