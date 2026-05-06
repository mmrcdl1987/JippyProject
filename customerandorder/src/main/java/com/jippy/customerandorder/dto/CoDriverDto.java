package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CoDriverDto {

    private Integer driverId;

    // Driver fields
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Size(max = 15)
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

//    extra added feilds to driver
@NotBlank(message = "Nominee name is required")
@Size(max = 50, message = "Nominee name must be less than 50 characters")
private String nomineeName;
    @NotBlank(message = "Nominee phone number is required")
    @Size(max = 15, message = "Nominee phone number must be less than 15 characters")
    @Pattern(regexp = "^[0-9]{10}$", message = "Nominee phone number must be 10 digits")
    private String nomineePhoneNumber;

    private Boolean isNomineeVerified;

    @NotBlank(message = "Family member name is required")
    @Size(max = 50, message = "Family member name must be less than 50 characters")
    private String familyMemberName;

    @NotBlank(message = "Family member phone number is required")
    @Size(max = 15, message = "Family member phone number must be less than 15 characters")
    @Pattern(regexp = "^[0-9]{10}$", message = "Family member phone number must be 10 digits")
    private String familyMemberPhoneNumber;

    private Boolean isFamilyMemberVerified;


    // KYC fields
    private Integer driverKycId;

    @NotBlank(message = "Aadhaar number is required")
    @Size(max = 20)
    private String aadharNumber;

    @NotBlank(message = "Driving license number is required")
    @Size(max = 20)
    private String drivingLicenseNumber;

    @NotBlank(message = "RC copy is required")
    @Size(max = 100)
    private String rcCopy;

    // Address fields
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @NotBlank(message = "Landmark is required")
    @Size(max = 150)
    private String landmark;

    @NotNull(message = "City id is required")
    private Integer cityId;

    @NotNull(message = "State id is required")
    private Integer stateId;

    @NotNull(message = "Area id is required")
    private Integer areaId;
}