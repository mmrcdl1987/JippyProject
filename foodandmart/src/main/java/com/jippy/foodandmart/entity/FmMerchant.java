package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "merchants", schema = "jippy_fm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMerchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Integer merchantId;

    @Column(name = "merchant_name", length = 150)
    private String merchantName;
    @Column(name = "merchant_email", length = 150, unique = true)
    private String merchantEmail;
    @Column(name = "merchant_phone", length = 20, unique = true)
    private String merchantPhone;
    @Column(name = "merchant_business_type", length = 50)
    private String merchantBusinessType;
    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "PENDING";
    @Column(name = "state", length = 30)
    private String state;
    @Column(name = "is_active", length = 1)
    private String isActive;
    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private Boolean isApproved = false;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "created_by")
    private Integer createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "profile_pic_url", length = 100)
    private String profilePicUrl;

    @JsonIgnore
    @OneToOne(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FmMerchantKyc kyc;

//    @ManyToOne
//    //@JoinColumn(name = "bank_details_bank_id")
    //@JsonIgnore
    //@OneToOne(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //private MerchantBankDetails bankDetails;

    @JsonIgnore
    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FmOutlet> outlets = new ArrayList<>();

    // Transient fields — carried in-memory during CSV/Excel import only
    @Transient
    private String firstName;
    @Transient
    private String lastName;
    @Transient
    private String dob;
    @Transient
    private String uploadedBy;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = "Y";
        if (this.status == null) this.status = "PENDING";
        if (this.isApproved == null) this.isApproved = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
