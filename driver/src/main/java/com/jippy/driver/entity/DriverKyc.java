package com.jippy.driver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_kyc", schema = "jippy_driver")
@Data
public class DriverKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverKycId;

    @NotBlank(message = "Aadhar number is required")
    @Size(max = 20, message = "Aadhar number must be less than 20 characters")
    @Column(name = "aadhar_number", length = 20, nullable = false)
    private String aadharNumber;

    @NotBlank(message = "PAN number is required")
    @Size(max = 20, message = "PAN number must be less than 20 characters")
    @Column(name = "pan_number", length = 20, nullable = false)
    private String panNumber;

    @NotBlank(message = "Driving license number is required")
    @Size(max = 20, message = "Driving license must be less than 20 characters")
    @Column(name = "driving_license_number", length = 20, nullable = false)
    private String drivingLicenseNumber;

    @NotBlank(message = "RC copy is required")
    @Size(max = 100, message = "RC copy must be less than 100 characters")
    @Column(name = "rc_copy", length = 100, nullable = false)
    private String rcCopy;

    @Size(max = 500, message = "Aadhar document URL must be less than 500 characters")
    @Column(name = "aadhar_number_url", length = 500)
    private String aadharDocUrl;

    @Size(max = 500, message = "PAN document URL must be less than 500 characters")
    @Column(name = "pan_number_url", length = 500)
    private String panDocUrl;

    @Size(max = 500, message = "Driving license document URL must be less than 500 characters")
    @Column(name = "driving_license_number_url", length = 500)
    private String drivingLicenseDocUrl;

    @Size(max = 500, message = "RC copy document URL must be less than 500 characters")
    @Column(name = "rc_copy_url", length = 500)
    private String rcCopyDocUrl;

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
    private Driver driver;
}
