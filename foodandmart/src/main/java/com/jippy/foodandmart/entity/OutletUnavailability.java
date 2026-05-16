package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "outlet_unavailability", schema = "jippy_fm")
public class OutletUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_unavailability_id")
    private Integer outletUnavailabilityId;

    @Column(name = "unavailability_id")
    private Integer unavailabilityId;

    @Column(name = "unavailability_from_date")
    private LocalDateTime unavailabilityFromDate;

    @Column(name = "unavailability_to_date")
    private LocalDateTime unavailabilityToDate;

    @Column(name = "type")
    private String type;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}