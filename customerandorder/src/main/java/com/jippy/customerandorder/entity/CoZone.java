package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

@Entity
@Table(name = "zones",schema = "jippy_customer_and_order")
@Data
public class CoZone {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer zoneId;
    private String zoneName;

    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon boundary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}
