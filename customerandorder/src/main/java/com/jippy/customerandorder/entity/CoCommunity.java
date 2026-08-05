package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="community", schema = "jippy_customer_and_order")
public class CoCommunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer communityId;
    private String communityName;
    private Integer communityAreaId;
    private String aboutCommunity;
    private String establishedYear;
    private String communityImageUrl;
    private Integer noOfFamilies;


    @Column(
            name = "community_boundary",
            columnDefinition = "geography(MultiPolygon, 4326)"
    )
    private Polygon boundary;

    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
