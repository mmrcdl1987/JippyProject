package com.jippy.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DriverDto {

    @Schema(description = "Unique identifier of the Driver.", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer driverId;

    // Driver fields
    @Schema(description = "Driver first name.", example = "Rohan")
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name can contain only letters and spaces")
    private String firstName;

    @Schema(description = "Driver last name.", example = "Vadluri")
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name can contain only letters and spaces")
    private String lastName;

    @Schema(description = "Driver mobile number.", example = "9876543210")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @Schema(description = "Driver email address.", example = "rohan@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email must be a valid Gmail address")
    private String email;

    //    extra added feilds to driver
    @Schema(description = "Nominee full name.", example = "Srinivas Bolishetti")
    @NotBlank(message = "Nominee name is required")
    @Size(max = 50, message = "Nominee name must be less than 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Nominee name can contain only letters and spaces")
    private String nomineeName;

    @Schema(description = "Nominee mobile number.", example = "9876543211")
    @NotBlank(message = "Nominee phone number is required")
    @Size(max = 15, message = "Nominee phone number must be less than 15 characters")
    @Pattern(regexp = "^[0-9]{10}$", message = "Nominee phone number must be 10 digits")
    private String nomineePhoneNumber;

    @Schema(description = "Indicates whether the nominee is verified.", example = "true")
    private Boolean isNomineeVerified;

    @Schema(description = "Family member full name.", example = "Suresh Kumar")
    @NotBlank(message = "Family member name is required")
    @Size(max = 50, message = "Family member name must be less than 50 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Family member name can contain only letters and spaces")
    private String familyMemberName;

    @Schema(description = "Family member mobile number.", example = "9876543212")
    @NotBlank(message = "Family member phone number is required")
    @Size(max = 15, message = "Family member phone number must be less than 15 characters")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Family member phone number must be a valid 10-digit Indian mobile number")
    private String familyMemberPhoneNumber;

    @Schema(description = "Indicates whether the family member is verified.", example = "false")
    private Boolean isFamilyMemberVerified;


    // KYC fields
    @Schema(description = "Unique identifier of Driver KYC.", example = "25", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer driverKycId;


    @Schema(description = "Driver Aadhaar number.", example = "123456789012")
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar number must contain exactly 12 digits")
    private String aadharNumber;

    @Schema(description = "Driver PAN number.", example = "ABCDE1234F")
    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number must be valid")
    private String panNumber;

    @Schema(description = "Driver driving license number.", example = "TS0920200012345")
    @NotBlank(message = "Driving license number is required ,Ex: TS0920200012345 , Driving Licence format")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[0-9]{11}$", message = "Invalid driving license number format")
    private String drivingLicenseNumber;


    @Schema(description = "Vehicle Registration Certificate (RC) number.", example = "TS09EF5678")
    @NotBlank(message = "RC copy Number is required , Ex: TS09EF5678  --RC ")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{1,4}$", message = "Invalid RC number format")
    private String rcCopy;

    // Document URL fields (read-only, populated after upload)
    @Schema(description = "Aadhar document URL.", example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/Documents/driver-12/aadhar-1234567890.pdf", accessMode = Schema.AccessMode.READ_ONLY)
    private String aadharDocUrl;

    @Schema(description = "PAN document URL.", example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/Documents/driver-12/pan-1234567890.pdf", accessMode = Schema.AccessMode.READ_ONLY)
    private String panDocUrl;

    @Schema(description = "Driving license document URL.", example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/Documents/driver-12/drivingLicense-1234567890.pdf", accessMode = Schema.AccessMode.READ_ONLY)
    private String drivingLicenseDocUrl;

    @Schema(description = "RC copy document URL.", example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/Documents/driver-12/rcCopy-1234567890.pdf", accessMode = Schema.AccessMode.READ_ONLY)
    private String rcCopyDocUrl;

    // Document file fields for upload (write-only)
    @Schema(description = "Aadhar document file (PDF, JPG, PNG)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile aadharDocument;

    @Schema(description = "PAN document file (PDF, JPG, PNG)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile panDocument;

    @Schema(description = "Driving license document file (PDF, JPG, PNG)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile drivingLicenseDocument;

    @Schema(description = "RC copy document file (PDF, JPG, PNG)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile rcCopyDocument;

    // Address fields in FM
    @Schema(description = "Building or house number.", example = "10-2-15")
    @NotBlank(message = "Building number is required")
    @Size(max = 50)
    private String buildingNumber;

    @Schema(description = "Road or street name.", example = "MG Road")
    @NotBlank(message = "Road is required")
    @Size(max = 100)
    private String road;

    @Schema(description = "Nearest landmark.", example = "Near Bus Stop")
    @NotBlank(message = "Landmark is required")
    @Size(max = 150)
    private String landmark;

    @Schema(description = "Unique identifier of the city.", example = "1")
    @NotNull(message = "City id is required")
    @Positive(message = "City ID must be greater than zero")
    private Integer cityId;

    @Schema(description = "Unique identifier of the state.", example = "36")
    @NotNull(message = "State id is required")
    @Positive(message = "State ID must be greater than zero")

    private Integer stateId;

    @Schema(description = "Unique identifier of the area.", example = "14")
    @NotNull(message = "Area id is required")
    @Positive(message = "Area ID must be greater than zero")
    private Integer areaId;

    @Schema(description = "Driver account password.", example = "Driver@123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$", message = "Password must contain at least one uppercase letter," + " one lowercase letter, one number and one special character")
    private String password;
}
