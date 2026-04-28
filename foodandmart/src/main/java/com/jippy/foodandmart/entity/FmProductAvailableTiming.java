package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "product_available_timings", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmProductAvailableTiming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_available_timing_id")
    private Integer productAvailableTimingId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private FmProduct product;

    /** 1=Mon … 7=Sun, or 0 to represent "all days" */
    @Column(name = "day_of_week_id", nullable = false)
    private Integer dayOfWeekId;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "created_by") private Integer createdBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private Integer updatedBy;

    @PrePersist public void prePersist() { this.createdAt = LocalDateTime.now(); }
    @PreUpdate  public void preUpdate()  { this.updatedAt = LocalDateTime.now(); }
}
