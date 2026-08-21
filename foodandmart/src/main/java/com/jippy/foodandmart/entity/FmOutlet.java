package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outlets", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmOutlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "outlet_name", length = 100, nullable = false)
    private String outletName;

    @Column(name = "outlet_pic_url")
    private String outletPicUrl;

    @Column(name = "outlet_email")
    private String outletEmail;
    @Column(name = "alternate_outlet_phone", length = 10)
    private String alternateOutletPhone;
    @Column(name = "merchant_id", nullable = false)
    private Integer merchantId;

    @Column(name = "outlet_phone", length = 20, nullable = false)
    private String outletPhone;
    @Column(name = "radius", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal radius = new BigDecimal("3.00");
    @Column(name = "subscription_status", length = 20)
    private String subscriptionStatus;
    @Column(name = "promotion_status", length = 20)
    private String promotionStatus;
    @Column(name = "review", precision = 2, scale = 1)
    private BigDecimal review;

    @Column(name = "is_active", length = 1)
    @Builder.Default
    private String isActive = "Y";

    @Column(name = "employee_id")
    private Integer employeeId;
    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    /**
     * oulet_location GEOGRAPHY(POINT, 4326) — note the DB column has a typo "oulet" (single-l),
     * kept intentionally to match the existing DDL.
     */
    @Column(name = "outlet_location", columnDefinition = "geography(Point,4326)")
    private Point outletLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", insertable = false, updatable = false)
    private FmMerchant merchant;

    @Column(name = "is_toggle")
    private Boolean isToggle;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cuisine_type", columnDefinition = "integer[]")
    private Integer[] cuisineType;


//    @JsonIgnore
//    @OneToOne(mappedBy = "outlet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private FmOutletAddress address;

    @JsonIgnore
    @OneToMany(mappedBy = "outlet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FmOutletDay> operatingDays = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "outlet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FmOutletCategory> categories = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = "Y";
        if (this.radius == null) this.radius = new BigDecimal("3.00");
        if (this.isApproved == null) this.isApproved = false;
        if (this.isToggle == null) this.isToggle = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
