package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "outlet_days", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_day_id")
    private Integer outletDayId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", insertable = false, updatable = false)
    private FmOutlet outlet;

    @Column(name = "day_of_week_id", nullable = false) private Integer dayOfWeekId;
    @Column(name = "is_open",        nullable = false) @Builder.Default private Boolean isOpen = true;
    @Column(name = "opening_time",   nullable = false) private LocalTime openingTime;
    @Column(name = "closing_time",   nullable = false) private LocalTime closingTime;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "created_by") private Integer createdBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private Integer updatedBy;

    @PrePersist public void prePersist() { this.createdAt = LocalDateTime.now(); }
    @PreUpdate  public void preUpdate()  { this.updatedAt = LocalDateTime.now(); }
}
