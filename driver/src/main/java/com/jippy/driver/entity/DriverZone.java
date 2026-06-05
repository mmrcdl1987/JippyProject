    package com.jippy.driver.entity;

    import jakarta.persistence.*;
    import lombok.Data;
    import org.locationtech.jts.geom.MultiPolygon;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "zones", schema = "jippy_driver")
    @Data
    public class DriverZone {

        // Primary key of zones table
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "zone_id")
        private Integer zoneId;

        // Name of the zone
        @Column(name = "zone_name")
        private String zoneName;

        // Polygon boundary of zone
        @Column(
                name = "boundary",
                columnDefinition = "geography(MultiPolygon, 4326)"
        )
        private MultiPolygon boundary;

        // Record creation timestamp
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        // User who created record
        @Column(name = "created_by")
        private Integer createdBy;

        // Record update timestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        // User who updated record
        @Column(name = "updated_by")
        private Integer updatedBy;
    }