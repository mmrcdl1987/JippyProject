package com.jippy.driver.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DriverDto {

    private Integer driverId;

    // Driver fields
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name can contain only letters and spaces")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name can contain only letters and spaces")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
            message = "Email must be a valid Gmail address"
    )
    private String email;

    //    extra added feilds to driver
    @NotBlank(message = "Nominee name is required")
    @Size(max = 50, message = "Nominee name must be less than 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Nominee name can contain only letters and spaces")
    private String nomineeName;

    @NotBlank(message = "Nominee phone number is required")
    @Size(max = 15, message = "Nominee phone number must be less than 15 characters")
    @Pattern(regexp = "^[0-9]{10}$", message = "Nominee phone number must be 10 digits")
    private String nomineePhoneNumber;

    private Boolean isNomineeVerified;

    @NotBlank(message = "Family member name is required")
    @Size(max = 50, message = "Family member name must be less than 50 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Family member name can contain only letters and spaces")
    private String familyMemberName;

    @NotBlank(message = "Family member phone number is required")
    @Size(max = 15, message = "Family member phone number must be less than 15 characters")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Family member phone number must be a valid 10-digit Indian mobile number")
    private String familyMemberPhoneNumber;

    private Boolean isFamilyMemberVerified;


    // KYC fields
    private Integer driverKycId;


    @NotBlank(message = "Aadhaar number is required")
    @Pattern(
            regexp = "^\\d{12}$",
            message = "Aadhaar number must contain exactly 12 digits")
    private String aadharNumber;

    @NotBlank(message = "Driving license number is required ,Ex: TS0920200012345 , Driving Licence format")
    @Pattern(
            regexp = "^[A-Z]{2}[0-9]{2}[0-9]{11}$",
            message = "Invalid driving license number format")
    private String drivingLicenseNumber;



    @NotBlank(message = "RC copy Number is required , Ex: TS09EF5678  --RC ")
    @Pattern(
            regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{1,4}$",
            message = "Invalid RC number format"
    )
    private String rcCopy;

    // Address fields in FM
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
    @Positive(message = "City ID must be greater than zero")
    private Integer cityId;

    @NotNull(message = "State id is required")
    @Positive(message = "State ID must be greater than zero")

    private Integer stateId;

    @NotNull(message = "Area id is required")
    @Positive(message = "Area ID must be greater than zero")
    private Integer areaId;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 20,
            message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must contain at least one uppercase letter," +
                    " one lowercase letter, one number and one special character")
    private String password;
}