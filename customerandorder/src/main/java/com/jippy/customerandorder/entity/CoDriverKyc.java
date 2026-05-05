package com.jippy.customerandorder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_kyc", schema = "jippy_customer_and_order")
@Data
public class CoDriverKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverKycId;

    @NotBlank(message = "Aadhar number is required")
    @Size(max = 20, message = "Aadhar number must be less than 20 characters")
    @Column(name = "aadhar_number", length = 20, nullable = false)
    private String aadharNumber;

    @NotBlank(message = "Driving license number is required")
    @Size(max = 20, message = "Driving license must be less than 20 characters")
    @Column(name = "driving_license_number", length = 20, nullable = false)
    private String drivingLicenseNumber;

    @NotBlank(message = "RC copy is required")
    @Size(max = 100, message = "RC copy must be less than 100 characters")
    @Column(name = "rc_copy", length = 100, nullable = false)
    private String rcCopy;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    // Owning side (controls foreign key)
    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private CoDriver driver;
}