package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "address", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutletAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

//    @Column(name = "outlet_id", nullable = false)
//    private Integer outletId;
//
//    @JsonIgnore
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "outlet_id", insertable = false, updatable = false)
//    private FmOutlet outlet;

    @Column(name = "jippy_address_id", nullable = false)
    private Integer jippyAddressId;
    @Column(name = "building_number", nullable = false)
    private String buildingNumber;
    @Column(name = "road", length = 100, nullable = false)
    private String road;
    @Column(name = "landmark", length = 150, nullable = false)
    private String landmark;
    @Column(name = "city_id", nullable = false)
    private Integer cityId;

    /**
     * FK to jippy_fm.states.state_id — resolved from state name during bulk upload
     */
    @Column(name = "state_id", nullable = false)
    private Integer stateId;

    /**
     * FK to jippy_fm.area.area_id — resolved from area name during bulk upload.
     *
     * <p>The upload sheet's ZipCode column accepts an area name string (e.g. "Banjara Hills").
     * The service looks this up in the area table and stores only the integer PK here.</p>
     */
    @Column(name = "area_id", nullable = false)
    private Integer areaId;

    @Column(name = "address_type", length = 50, nullable = false)
    private String addressType;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(
            name = "location",
            columnDefinition = "geography(Point,4326)"
    )
    private Point location;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
