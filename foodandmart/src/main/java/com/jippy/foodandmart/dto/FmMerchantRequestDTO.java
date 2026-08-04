package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMerchantRequestDTO {
// ── Required Fields ──────────────────────────────────────────────────────

    @Schema(description = "Merchant's first name", example = "Rohan")
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 75, message = "First name must be between 2 and 75 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters")
    private String firstName;

    @Schema(description = "Merchant's last name", example = "Vadluri")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 75, message = "Last name must be between 2 and 75 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters")
    private String lastName;


    @Schema(description = "Merchant's email address", example = "rohan@gmail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Schema(description = "Merchant's mobile number", example = "9876543210")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit Indian mobile number")
    private String phone;

    @Schema(description = "Merchant portal username", example = "rohan123")
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @Schema(description = "Merchant portal password", example = "Rohan@123")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must contain uppercase, lowercase, number and special character")
    private String password;

    @Schema(description = "Business or outlet type", example = "Restaurant")
    @NotBlank(message = "Outlet type is required")
    @Size(max = 50, message = "Outlet type must not exceed 50 characters")
    private String outletType;

    @Schema(description = "Name of the user who uploaded the merchant details", example = "Admin")
    @Size(max = 100, message = "UploadedBy must not exceed 100 characters")
    private String uploadedBy;

    @Schema(description = "Merchant PAN number", example = "ABCDE1234F")
    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be in format: AAAAA9999A")
    private String pan;

    @Schema(description = "Merchant Aadhaar number", example = "987654321012")
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Aadhaar must be a valid 12-digit number")
    private String adhar;

    @Schema(description = "Merchant bank account number", example = "1234567890123456")
    @Pattern(regexp = "^$|^[0-9]{9,18}$", message = "Account number must be 9–18 digits")
    private String accountNumber;

    @Schema(description = "Bank IFSC code", example = "SBIN0001234")
    @Pattern(regexp = "^$|^[A-Z]{4}0[A-Z0-9]{6}$", message = "IFSC must be in format: ABCD0123456")
    private String ifscCode;

    @Schema(description = "Branch or bank location", example = "Kukatpally Branch")
    @Size(max = 100, message = "Bank location must not exceed 100 characters")
    private String bankLocation;

    @Schema(description = "Account holder name as per bank records", example = "Rohan Vadluri")
    @Size(max = 150, message = "Name in bank account must not exceed 150 characters")
    private String nameInBankAccount;


// ── Optional Fields ───────────────────────────────────────────────────────
    @Schema(description = "Merchant's date of birth [OPTIONAL- FEILD]", example = "2002-08-15")
    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2}|\\d{2}-\\d{2}-\\d{2})$", message = "DOB must be in YYYY-MM-DD or MM-DD-YY format")
    private String dob;

    @Schema(description = "FSSAI license number [OPTIONAL- FEILD]", example = "12345678901234")
    @Pattern(regexp = "^\\d{14}$", message = "FSSAI must be a 14-digit number")
    private String fssai;

    @Schema(description = "GST Identification Number (GSTIN) [OPTIONAL- FEILD]", example = "36ABCDE1234F1Z5")
    @Pattern(regexp = "^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "GST must be a valid 15-character GSTIN")
    private String gstNumber;


}
