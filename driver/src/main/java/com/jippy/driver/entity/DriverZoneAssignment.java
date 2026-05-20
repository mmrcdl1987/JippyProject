package com.jippy.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "driver_zone_assignment",
        schema = "jippy_driver"
)
@Getter
@Setter
public class DriverZoneAssignment {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_zone_assignment_id")
    private Integer driverZoneAssignmentId;

    // Mapping with existing driver entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    // Mapping with zones table
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private DriverZone zone;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}