package com.jippy.driver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "driver", schema = "jippy_driver")
@Getter
@Setter
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer driverId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 15, message = "Phone number must be less than 15 characters")
    @Column(name = "phone_number", length = 15, nullable = false, unique = true)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100)
    private String email;

//    newly added feilds after changing requirement
@Column(name = "nominee_name", length = 50)
private String nomineeName;

    @Column(name = "nominee_phone_number", length = 15)
    private String nomineePhoneNumber;

    @Column(name = "is_nominee_verified")
    private Boolean isNomineeVerified; // DB default FALSE

    @Column(name = "family_member_name", length = 50)
    private String familyMemberName;

    @Column(name = "family_member_phone_number", length = 15)
    private String familyMemberPhoneNumber;

    @Column(name = "is_family_member_verified")
    private Boolean isFamilyMemberVerified; // DB default FALSE

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
    /**
     * Indicates whether the Driver has been approved.
     * Default value is FALSE.
     */
    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "ready_to_accept_orders")
    private Boolean readyToAcceptOrders=false;

    // One-to-One mapping with KYC
    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private DriverKyc driverKyc;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<DriverOrder> driverOrders;

    @Column(name = "profile_pic_url")
    private String profilePicUrl;
}